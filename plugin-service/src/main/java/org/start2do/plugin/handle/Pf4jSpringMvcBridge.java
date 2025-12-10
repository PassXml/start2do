package org.start2do.plugin.handle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.start2do.plugin.api.spring.SpringMvcControllerExtension;
import org.start2do.plugin.api.spring.SpringPluginBeansExtension;

/**
 * PF4J 与 Spring MVC 的桥接组件
 * <p>
 * 职责： 1. 监听插件生命周期事件（启动 / 停止） 2. 在插件启动时，将插件声明的 Controller 类动态注册为 Spring MVC 控制器 3. 在插件停止时，从 Spring MVC 中移除对应的
 * RequestMapping，并销毁 Controller Bean
 * <p>
 * 注意： - 该组件不依赖 pf4j-spring，仅使用 PF4J 核心 API - 插件需要实现 {@link SpringMvcControllerExtension} 来声明要暴露的 Controller 类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Pf4jSpringMvcBridge implements PluginStateListener {

    /**
     * PF4J 插件管理器，由 PluginAutoPluginConfiguration 或外部桥接模块提供
     */
    private final PluginManager pluginManager;

    /**
     * Spring 应用上下文，用于获取 BeanFactory 和 HandlerMapping
     */
    private final ConfigurableApplicationContext applicationContext;

    /**
     * 记录每个插件注册过的 RequestMapping，用于插件关闭时反注册
     */
    private final Map<String, List<RequestMappingInfo>> pluginMappings = new ConcurrentHashMap<>();

    /**
     * 记录每个插件动态注册的 Controller Bean 名称，便于销毁
     */
    private final Map<String, List<String>> pluginBeanNames = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (pluginManager == null) {
            log.info("Pf4jSpringMvcBridge 初始化: 未找到 PluginManager，跳过 Spring MVC 动态注册");
            return;
        }

        // 注册插件状态监听器
        pluginManager.addPluginStateListener(this);

        // 对于已经处于 STARTED 状态的插件，补偿性注册一次 Controller
        for (PluginWrapper wrapper : pluginManager.getPlugins()) {
            if (wrapper.getPluginState() == PluginState.STARTED) {
                registerPluginControllers(wrapper.getPluginId());
            }
        }

        log.info("Pf4jSpringMvcBridge 初始化完成");
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        // 兼容旧版本 PF4J：集中在一个回调里处理状态变化
        String pluginId = event.getPlugin().getPluginId();
        PluginState state = event.getPluginState();
        if (state == PluginState.STARTED) {
            registerPluginControllers(pluginId);
        } else if (state == PluginState.UNLOADED || state == PluginState.STOPPED || state == PluginState.DISABLED
            || state == PluginState.FAILED) {
            unregisterPluginControllers(pluginId);
        }
    }

    /**
     * 注册指定插件声明的所有 Bean 与 Controller
     */
    private void registerPluginControllers(String pluginId) {
        RequestMappingHandlerMapping handlerMapping =
            applicationContext.getBean(RequestMappingHandlerMapping.class);
        AutowireCapableBeanFactory acf = applicationContext.getAutowireCapableBeanFactory();
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

        List<String> beanNames = new ArrayList<>();
        List<RequestMappingInfo> mappings = new ArrayList<>();

        // 先注册插件内部声明的普通 Bean（Service / Component 等）
        // 这些 Bean 可能会被 Controller 注入，所以必须先于 Controller 创建
        List<SpringPluginBeansExtension> beanExtensions =
            pluginManager.getExtensions(SpringPluginBeansExtension.class, pluginId);
        for (SpringPluginBeansExtension be : beanExtensions) {
            for (Class<?> beanClass : be.getBeanClasses()) {
                String beanName = buildBeanName(pluginId, beanClass);
                try {
                    Object bean = acf.createBean(beanClass);
                    beanFactory.registerSingleton(beanName, bean);
                    beanNames.add(beanName);
                    log.info("插件 {} 注册 Bean 成功: beanName={}, class={}",
                        pluginId, beanName, beanClass.getName());
                } catch (Exception e) {
                    log.error("插件 {} 注册 Bean 失败: class={}", pluginId, beanClass.getName(), e);
                    pluginManager.unloadPlugin(pluginId);
                    return;
                }
            }
        }

        // 这里是关键点：
        // 1. 宿主只需要“知道扩展点接口类型” SpringMvcControllerExtension.class
        // 2. PF4J 会返回所有插件中实现了该扩展点的实例（每个插件可以有 0~N 个实现）
        // 3. 宿主不关心“具体是哪个插件”“具体实现类叫什么”，只按接口能力来消费
        List<SpringMvcControllerExtension> extensions =
            pluginManager.getExtensions(SpringMvcControllerExtension.class, pluginId);

        if (extensions.isEmpty()) {
            log.info("插件 {} 未提供 SpringMvcControllerExtension，跳过 Controller 注册", pluginId);
            // 即使没有 Controller，也需要记录前面已注册的普通 Bean，方便插件卸载时销毁
            if (!beanNames.isEmpty()) {
                pluginBeanNames.put(pluginId, beanNames);
            }
            return;
        }

        for (SpringMvcControllerExtension ext : extensions) {
            // 每个扩展实例代表“某个插件告诉宿主：我有一批需要注册的 Controller 类”
            // 宿主只依赖接口方法 getControllerClasses，不依赖具体实现类
            for (Class<?> controllerClass : ext.getControllerClasses()) {
                String beanName = buildBeanName(pluginId, controllerClass);
                try {
                    // 使用 Spring 的工厂方法创建 Bean，确保 @Autowired、AOP 等功能生效
                    Object controllerBean = acf.createBean(controllerClass);
                    beanFactory.registerSingleton(beanName, controllerBean);

                    // 注册 RequestMapping（detectHandlerMethods 是受保护方法，这里通过反射调用）
                    invokeDetectHandlerMethods(handlerMapping, beanName);

                    // 记录该 Bean 对应的所有 RequestMappingInfo，后续卸载时使用
                    List<RequestMappingInfo> infos = findMappingsByBeanName(handlerMapping, beanName);
                    mappings.addAll(infos);
                    beanNames.add(beanName);

                    log.info("插件 {} 注册 Controller 成功: beanName={}, class={}, mappings={}",
                        pluginId, beanName, controllerClass.getName(), infos.size());
                } catch (Exception e) {
                    log.error("插件 {} 注册 Controller 失败: class={}", pluginId, controllerClass.getName(), e);
                    pluginManager.unloadPlugin(pluginId);
                    unregisterPluginControllers(pluginId);
                    return;
                }
            }
        }

        if (!beanNames.isEmpty()) {
            pluginBeanNames.put(pluginId, beanNames);
        }
        if (!mappings.isEmpty()) {
            pluginMappings.put(pluginId, mappings);
        }
    }

    /**
     * 反注册指定插件的所有 Controller
     */
    private void unregisterPluginControllers(String pluginId) {
        RequestMappingHandlerMapping handlerMapping =
            applicationContext.getBean(RequestMappingHandlerMapping.class);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

        // 先移除 RequestMapping
        // 注意：这里同样不需要关心“有哪些插件实现了扩展点”，
        // 只根据插件 ID 把之前为该插件记录过的映射全部移除即可
        List<RequestMappingInfo> infos = pluginMappings.remove(pluginId);
        if (infos != null) {
            for (RequestMappingInfo info : infos) {
                handlerMapping.unregisterMapping(info);
            }
            log.info("插件 {} 取消注册 RequestMapping 数量: {}", pluginId, infos.size());
        }

        // 再销毁 Controller Bean
        List<String> beanNames = pluginBeanNames.remove(pluginId);
        if (beanNames != null) {
            for (String beanName : beanNames) {
                if (beanFactory.containsSingleton(beanName)) {
                    try {
                        // 优先尝试调用 destroySingleton(String)（如果当前 Spring 版本提供）
                        java.lang.reflect.Method destroySingleton =
                            ReflectionUtils.findMethod(beanFactory.getClass(), "destroySingleton", String.class);
                        if (destroySingleton != null) {
                            ReflectionUtils.makeAccessible(destroySingleton);
                            destroySingleton.invoke(beanFactory, beanName);
                        } else {
                            // 回退方案：先按常规方式销毁 Bean，再尝试调用受保护的 removeSingleton(String)
                            Object bean = beanFactory.getBean(beanName);
                            beanFactory.destroyBean(bean);

                            java.lang.reflect.Method removeSingleton =
                                ReflectionUtils.findMethod(beanFactory.getClass(), "removeSingleton", String.class);
                            if (removeSingleton != null) {
                                ReflectionUtils.makeAccessible(removeSingleton);
                                removeSingleton.invoke(beanFactory, beanName);
                            }
                        }
                        log.info("插件 {} 销毁 Controller Bean: {}", pluginId, beanName);
                    } catch (Exception ex) {
                        // 避免因销毁失败影响主流程，这里只记录告警
                        log.warn("插件 {} 销毁 Controller Bean 失败(忽略继续): beanName={}", pluginId, beanName, ex);
                    }
                }
            }
        }
    }

    /**
     * 根据 Bean 名称查找所有已注册的 RequestMappingInfo
     */
    private List<RequestMappingInfo> findMappingsByBeanName(RequestMappingHandlerMapping handlerMapping,
        String beanName) {
        List<RequestMappingInfo> result = new ArrayList<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            Object bean = entry.getValue().getBean();
            // 通过 beanName 匹配（detectHandlerMethods(String beanName) 场景下，getBean() 返回的就是 beanName）
            if (beanName.equals(bean)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * 构造插件 Controller 在 Spring 容器中的 Bean 名称
     */
    private String buildBeanName(String pluginId, Class<?> controllerClass) {
        return "plugin_" + pluginId + "_" + controllerClass.getName();
    }

    /**
     * 通过反射调用 RequestMappingHandlerMapping.detectHandlerMethods
     * <p>
     * 该方法在 Spring 中是 protected，不能直接从外部调用，只能通过反射或自定义子类暴露。
     */
    private void invokeDetectHandlerMethods(RequestMappingHandlerMapping handlerMapping, String beanName) {
        try {
            // 使用 Spring 提供的工具从类及其父类中查找方法，兼容不同版本
            java.lang.reflect.Method method =
                ReflectionUtils.findMethod(handlerMapping.getClass(), "detectHandlerMethods", Object.class);
            if (method == null) {
                throw new IllegalStateException(
                    "在 RequestMappingHandlerMapping 及其父类中未找到 detectHandlerMethods(Object) 方法");
            }
            ReflectionUtils.makeAccessible(method);
            method.invoke(handlerMapping, beanName);
        } catch (Exception e) {
            throw new IllegalStateException("调用 RequestMappingHandlerMapping.detectHandlerMethods 失败", e);
        }
    }
}
