package org.start2do.plugin.handle;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.start2do.plugin.api.pf4j.Pf4jBridge;
import org.start2do.plugin.api.spring.PluginSpringBeanUtils;
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
public class Pf4jSpringMvcBridge implements Pf4jBridge {

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
        log.info("Pf4jSpringMvcBridge 初始化完成，将由 Pf4jBridgeOrchestrator 统一编排触发");
    }

    @Override
    public int getOrder() {
        // 建议顺序：DataSource(100) → Mapper(200) → MVC(300) → SOAP(400)
        return 300;
    }

    @Override
    public void onPluginStarted(String pluginId) {
        registerPluginControllers(pluginId);
    }

    @Override
    public void onPluginStopped(String pluginId) {
        unregisterPluginControllers(pluginId);
    }

    /**
     * 注册指定插件声明的所有 Bean 与 Controller
     */
    private void registerPluginControllers(String pluginId) {
        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        AutowireCapableBeanFactory acf = applicationContext.getAutowireCapableBeanFactory();

        // 关键：让 Spring AOP/CGLIB 在插件 ClassLoader 下生成代理类，否则会出现 Enhancer 类在宿主 ClassLoader 下无法解析插件类的问题
        PluginWrapper plugin = pluginManager.getPlugin(pluginId);
        ClassLoader pluginClassLoader = plugin == null ? null : plugin.getPluginClassLoader();
        ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();
        ClassLoader oldBeanCl = beanFactory.getBeanClassLoader();
        ClassLoader oldTempCl = beanFactory.getTempClassLoader();
        Map<String, AbstractAutoProxyCreator> oldAutoProxyCreators = Collections.emptyMap();
        if (pluginClassLoader != null) {
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            beanFactory.setBeanClassLoader(pluginClassLoader);
            beanFactory.setTempClassLoader(pluginClassLoader);
            // 同步切换 AOP AutoProxyCreator 使用的 classloader（它们会缓存 proxyClassLoader，否则仍可能用宿主 AppClassLoader 生成 CGLIB 类）
            try {
                oldAutoProxyCreators = applicationContext.getBeansOfType(AbstractAutoProxyCreator.class);
                for (AbstractAutoProxyCreator apc : oldAutoProxyCreators.values()) {
                    apc.setBeanClassLoader(pluginClassLoader);
                    apc.setProxyClassLoader(pluginClassLoader);
                }
                log.info("插件 {} 绑定 ClassLoader: pluginCl={}, autoProxyCreators={}", pluginId,
                    pluginClassLoader.getClass().getName(), oldAutoProxyCreators.size());
            } catch (Exception ex) {
                log.warn("插件 {} 切换 AutoProxyCreator ClassLoader 失败(忽略继续，可能仍会触发 CGLIB 代理问题)",
                    pluginId, ex);
            }
        }

        try {
            // 方案2：先注册所有 BeanDefinition，再触发实例化，让 Spring 按依赖图解析实例化顺序
            if (!(beanFactory instanceof BeanDefinitionRegistry)) {
                log.warn(
                    "当前 BeanFactory 不支持 BeanDefinitionRegistry，将回退到旧的 createBean + registerSingleton 方式: {}",
                    beanFactory.getClass().getName());
                registerPluginControllersLegacy(pluginId, handlerMapping, acf, beanFactory);
                return;
            }
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

            List<String> springBeanNames = new ArrayList<>();
            Set<String> pluginBeanClassNames = new LinkedHashSet<>();
            List<RequestMappingInfo> mappings = new ArrayList<>();

            // 1) 收集并注册插件普通 Bean 的 BeanDefinition
            List<SpringPluginBeansExtension> beanExtensions =
                pluginManager.getExtensions(SpringPluginBeansExtension.class, pluginId);
            for (SpringPluginBeansExtension be : beanExtensions) {
                for (Class<?> beanClass : be.getBeanClasses()) {
                    String beanName = PluginSpringBeanUtils.buildPluginBeanName(pluginId, beanClass);
                    if (!registerOrReplaceBeanDefinition(registry, beanName, beanClass)) {
                        unloadAndCleanup(pluginId);
                        return;
                    }
                    springBeanNames.add(beanName);
                    pluginBeanClassNames.add(beanClass.getName());
                    pluginBeanNames.computeIfAbsent(pluginId, k -> new ArrayList<String>()).add(beanName);
                }
            }

            // 2) 收集并注册插件 Controller 的 BeanDefinition（先注册后实例化，保证依赖可解析）
            List<SpringMvcControllerExtension> controllerExts =
                pluginManager.getExtensions(SpringMvcControllerExtension.class, pluginId);
            for (SpringMvcControllerExtension ext : controllerExts) {
                for (Class<?> controllerClass : ext.getControllerClasses()) {
                    String beanName = PluginSpringBeanUtils.buildPluginBeanName(pluginId, controllerClass);
                    if (!registerOrReplaceBeanDefinition(registry, beanName, controllerClass)) {
                        unloadAndCleanup(pluginId);
                        return;
                    }
                    pluginBeanNames.computeIfAbsent(pluginId, k -> new ArrayList<String>()).add(beanName);
                }
            }

            // 3) 触发普通 Bean 实例化（可选：保持原行为，提前暴露问题）
            if (!instantiatePluginBeansWithRetry(pluginId, springBeanNames, pluginBeanClassNames)) {
                return;
            }
            // 3.1) 补齐动态 @EventListener（BeanDefinitionRegistry 注册场景下，Spring 不会再次触发 EventListenerMethodProcessor 扫描）
            registerEventListenersForExistingBeans(pluginId, springBeanNames);

            // 4) 触发 Controller 实例化并注册 RequestMapping（实例化失败会直接中止插件启动）
            for (SpringMvcControllerExtension ext : controllerExts) {
                for (Class<?> controllerClass : ext.getControllerClasses()) {
                    String beanName = PluginSpringBeanUtils.buildPluginBeanName(pluginId, controllerClass);

                    try {
                        validateControllerMappingConflicts(controllerClass, handlerMapping, pluginId);
                    } catch (IllegalStateException conflict) {
                        log.error("插件 {} 注册 Controller 失败，检测到路径冲突: class={}, msg={}", pluginId,
                            controllerClass.getName(), conflict.getMessage());
                        unloadAndCleanup(pluginId);
                        return;
                    }

                    try {
                        // 确保在插件 ClassLoader 上下文中实例化 Bean，避免 LocalVariableTableParameterNameDiscoverer 无法解析方法签名中的插件类
                        ClassLoader currentTccl = Thread.currentThread().getContextClassLoader();
                        if (pluginClassLoader != null && !pluginClassLoader.equals(currentTccl)) {
                            log.warn("插件 {} 实例化 Controller 时 TCCL 不一致，当前={}, 插件={}, 将重新设置",
                                pluginId, currentTccl, pluginClassLoader);
                            Thread.currentThread().setContextClassLoader(pluginClassLoader);
                        }

                        Object controllerBean = applicationContext.getBean(beanName);
                        PluginSpringBeanUtils.registerEventListenerAdaptersForExistingBeanIfNecessary(pluginId, beanName,
                            controllerBean, applicationContext, pluginBeanNames);
                        registerRequestMappingsForController(pluginId, handlerMapping, beanName, controllerClass,
                            mappings);
                    } catch (Exception e) {
                        // 增强错误诊断：检查是否是 LocalVariableTableParameterNameDiscoverer 导致的类加载问题
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("cannot be resolved in the class object")) {
                            logControllerClassDiagnostics(pluginId, controllerClass);
                            log.error("插件 {} 注册 Controller 失败: class={}. " +
                                    "检测到方法解析失败，可能原因: " +
                                    "1) Controller 方法签名中的参数/返回值类型在插件 ClassLoader 中不可见; " +
                                    "2) 请确保所有依赖类都包含在插件的 dependencies.txt 或正确配置了类加载策略; " +
                                    "3) 建议插件编译开启 -parameters；或在 Controller 构造函数上添加 @ConstructorProperties / @Autowired 明确标注；"
                                    +
                                    "4) 检查是否存在同名类多份（.class 资源与已加载 Class 不一致）",
                                pluginId, controllerClass.getName(), e);
                        } else {
                            log.error("插件 {} 注册 Controller 失败: class={}", pluginId, controllerClass.getName(), e);
                        }
                        unloadAndCleanup(pluginId);
                        return;
                    }
                }
            }

            // 3. 统一记录插件级别的 RequestMapping，方便卸载时集中清理
            if (!mappings.isEmpty()) {
                pluginMappings.put(pluginId, mappings);
            }
        } finally {
            if (pluginClassLoader != null) {
                // 恢复现场，避免影响宿主后续逻辑
                try {
                    for (AbstractAutoProxyCreator apc : oldAutoProxyCreators.values()) {
                        apc.setBeanClassLoader(oldBeanCl);
                        apc.setProxyClassLoader(oldBeanCl);
                    }
                } catch (Exception ex) {
                    log.warn("插件 {} 恢复 AutoProxyCreator ClassLoader 失败(忽略继续)", pluginId, ex);
                }
                beanFactory.setTempClassLoader(oldTempCl);
                beanFactory.setBeanClassLoader(oldBeanCl);
                Thread.currentThread().setContextClassLoader(oldTccl);
            }
        }
    }

    /**
     * 实例化插件普通 Bean：对“插件内部 Bean 之间的依赖”做一次或多次重试。
     * <p>
     * 背景：插件 BeanDefinition 是动态注册的，部分 Spring 版本/场景下，按类型解析依赖时可能暂时无法从 BeanDefinition 中预测出候选类型； 此时若先实例化依赖方（例如
     * ScheduledTask），会抛 NoSuchBeanDefinitionException。通过“先尽量实例化其他 Bean，再回头重试”的方式， 可以在不改变插件声明顺序的情况下，让依赖方在依赖 Bean
     * 已作为单例存在后成功注入。
     */
    private boolean instantiatePluginBeansWithRetry(String pluginId, List<String> beanNames,
        Set<String> pluginBeanClassNames) {
        if (beanNames == null || beanNames.isEmpty()) {
            return true;
        }

        List<String> pending = new ArrayList<>(beanNames);
        Map<String, String> lastMissingTypeByBeanName = new ConcurrentHashMap<>();

        // 最多循环 N 轮：每轮至少要成功实例化 1 个 Bean，否则直接判定无法收敛
        int maxPasses = pending.size();
        for (int pass = 1; pass <= maxPasses; pass++) {
            boolean progressed = false;
            for (java.util.Iterator<String> it = pending.iterator(); it.hasNext(); ) {
                String beanName = it.next();
                try {
                    applicationContext.getBean(beanName);
                    it.remove();
                    progressed = true;
                } catch (BeansException ex) {
                    // 仅当“缺失的依赖类型”属于本插件声明的 Bean 时，才做重试；否则视为真正配置问题，立即失败
                    String missingTypeName = findMissingBeanTypeName(ex);
                    if (missingTypeName != null && pluginBeanClassNames.contains(missingTypeName)) {
                        lastMissingTypeByBeanName.put(beanName, missingTypeName);
                        continue;
                    }
                    log.error("插件 {} 创建普通 Bean 失败: beanName={}, 卸载插件", pluginId, beanName, ex);
                    unloadAndCleanup(pluginId);
                    return false;
                }
            }

            if (pending.isEmpty()) {
                return true;
            }
            if (!progressed) {
                break;
            }
        }

        log.error("插件 {} 创建普通 Bean 失败: 仍有 {} 个 Bean 依赖未满足，将卸载插件; pending={}", pluginId,
            pending.size(), pending);
        if (!lastMissingTypeByBeanName.isEmpty()) {
            log.error("插件 {} 普通 Bean 依赖缺失详情(仅插件内依赖会重试): {}", pluginId, lastMissingTypeByBeanName);
        }
        unloadAndCleanup(pluginId);
        return false;
    }

    private void registerEventListenersForExistingBeans(String pluginId, List<String> beanNames) {
        if (beanNames == null || beanNames.isEmpty()) {
            return;
        }
        log.info("插件 {} 动态补齐 @EventListener: beans={}", pluginId, beanNames.size());
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                PluginSpringBeanUtils.registerEventListenerAdaptersForExistingBeanIfNecessary(pluginId, beanName, bean,
                    applicationContext, pluginBeanNames);
            } catch (Exception ex) {
                log.warn("插件 {} 动态补齐 @EventListener 失败(忽略继续): beanName={}", pluginId, beanName, ex);
            }
        }
    }

    private String findMissingBeanTypeName(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof NoSuchBeanDefinitionException) {
                NoSuchBeanDefinitionException nsb = (NoSuchBeanDefinitionException) cur;
                if (nsb.getBeanType() != null) {
                    return nsb.getBeanType().getName();
                }
                if (nsb.getResolvableType() != null && nsb.getResolvableType().resolve() != null) {
                    return nsb.getResolvableType().resolve().getName();
                }
                return null;
            }
            cur = cur.getCause();
        }
        return null;
    }

    /**
     * 旧方案：直接 createBean 并 registerSingleton。
     * <p>
     * 当 BeanFactory 不支持 BeanDefinitionRegistry 时作为降级（少见）。
     */
    private void registerPluginControllersLegacy(String pluginId,
        RequestMappingHandlerMapping handlerMapping,
        AutowireCapableBeanFactory acf,
        ConfigurableListableBeanFactory beanFactory) {
        List<String> beanNames = new ArrayList<>();
        List<RequestMappingInfo> mappings = new ArrayList<>();
        if (!registerPluginBeans(pluginId, acf, beanFactory, beanNames)) {
            return;
        }
        if (!registerPluginControllerBeans(pluginId, handlerMapping, acf, beanFactory, beanNames, mappings)) {
            return;
        }
        if (!mappings.isEmpty()) {
            pluginMappings.put(pluginId, mappings);
        }
    }

    private boolean registerOrReplaceBeanDefinition(BeanDefinitionRegistry registry, String beanName,
        Class<?> beanClass) {
        try {
            if (registry.containsBeanDefinition(beanName)) {
                registry.removeBeanDefinition(beanName);
            }
            RootBeanDefinition bd = new RootBeanDefinition(beanClass);
            PluginSpringBeanUtils.applyLifecycleMetadata(bd, beanClass);

            // 关键修复：设置 BeanDefinition 使用当前的 BeanClassLoader（即插件 ClassLoader），
            // 确保 LocalVariableTableParameterNameDiscoverer 在解析方法签名时能够正确加载插件类
            ClassLoader currentBeanCl = ((ConfigurableListableBeanFactory) registry).getBeanClassLoader();
            if (currentBeanCl != null) {
                bd.setBeanClass(beanClass);
                // 确保 BeanDefinition 知道应该使用哪个 ClassLoader 来解析类型
                bd.setAttribute("PLUGIN_CLASS_LOADER", currentBeanCl);
            }

            registry.registerBeanDefinition(beanName, bd);
            return true;
        } catch (Exception ex) {
            log.error("注册插件 BeanDefinition 失败: beanName={}, class={}", beanName, beanClass.getName(), ex);
            return false;
        }
    }

    private void unloadAndCleanup(String pluginId) {
        try {
            pluginManager.unloadPlugin(pluginId);
        } catch (Exception ex) {
            log.warn("插件 {} 卸载失败(忽略继续清理): {}", pluginId, ex.getMessage(), ex);
        }
        unregisterPluginControllers(pluginId);
    }

    private void logControllerClassDiagnostics(String pluginId, Class<?> controllerClass) {
        if (controllerClass == null) {
            return;
        }
        try {
            ClassLoader cl = controllerClass.getClassLoader();
            String resourcePath = controllerClass.getName().replace('.', '/') + ".class";
            String codeSource = null;
            try {
                if (controllerClass.getProtectionDomain() != null
                    && controllerClass.getProtectionDomain().getCodeSource() != null
                    && controllerClass.getProtectionDomain().getCodeSource().getLocation() != null) {
                    codeSource = controllerClass.getProtectionDomain().getCodeSource().getLocation().toString();
                }
            } catch (Exception ignore) {
                // 忽略：不同安全策略下可能不可访问
            }

            List<String> resources = new ArrayList<>();
            if (cl != null) {
                Enumeration<URL> urls = cl.getResources(resourcePath);
                while (urls != null && urls.hasMoreElements()) {
                    URL u = urls.nextElement();
                    if (u != null) {
                        resources.add(u.toString());
                    }
                }
            }
            log.error("插件 {} Controller 类诊断: class={}, classLoader={}, codeSource={}, classResources={}",
                pluginId, controllerClass.getName(),
                cl == null ? "<null>" : cl.getClass().getName(),
                codeSource == null ? "<unknown>" : codeSource,
                resources.isEmpty() ? "<empty>" : resources);
        } catch (Exception ex) {
            log.warn("插件 {} Controller 类诊断失败(忽略继续): class={}", pluginId, controllerClass.getName(), ex);
        }
    }

    /**
     * 反注册指定插件的所有 Controller
     */
    private void unregisterPluginControllers(String pluginId) {
        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

        // 先移除 RequestMapping，再销毁 Bean，保证 HandlerMapping 不再引用即将销毁的 Bean
        unregisterPluginRequestMappings(pluginId, handlerMapping);
        destroyPluginBeans(pluginId, beanFactory);
    }

    /**
     * 注册插件中声明的 Service/Component 等普通 Bean
     */
    private boolean registerPluginBeans(String pluginId, AutowireCapableBeanFactory acf,
        ConfigurableListableBeanFactory beanFactory, List<String> beanNames) {
        // 这些 Bean 可能会被 Controller 注入，所以必须先于 Controller 创建
        List<SpringPluginBeansExtension> beanExtensions = pluginManager.getExtensions(SpringPluginBeansExtension.class,
            pluginId);
        log.info("插件 注册BeanSize： {}", beanExtensions.size());
        for (SpringPluginBeansExtension be : beanExtensions) {
            for (Class<?> beanClass : be.getBeanClasses()) {
                String beanName = PluginSpringBeanUtils.buildPluginBeanName(pluginId, beanClass);
                try {
                    Object bean = acf.createBean(beanClass);
                    PluginSpringBeanUtils.registerPluginBean(pluginId, beanName, bean, applicationContext,
                        pluginBeanNames);
                    log.info("插件 {} 注册 Bean 成功: beanName={}, class={}", pluginId, beanName, beanClass.getName());
                } catch (Exception e) {
                    log.error("插件 {} 注册 Bean 失败: class={}, 卸载插件", pluginId, beanClass.getName(), e);
                    unregisterPluginControllers(pluginId);
                    pluginManager.unloadPlugin(pluginId);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 注册插件提供的 Controller 及其对应的 RequestMapping
     */
    private boolean registerPluginControllerBeans(String pluginId, RequestMappingHandlerMapping handlerMapping,
        AutowireCapableBeanFactory acf, ConfigurableListableBeanFactory beanFactory, List<String> beanNames,
        List<RequestMappingInfo> mappings) {

        // 1. 获取插件实现的 SpringMvcControllerExtension 扩展点
        List<SpringMvcControllerExtension> extensions = pluginManager.getExtensions(SpringMvcControllerExtension.class,
            pluginId);

        if (extensions.isEmpty()) {
            log.info("插件 {} 未提供 SpringMvcControllerExtension，跳过 Controller 注册", pluginId);
            return true;
        }

        // 2. 针对每个扩展点声明的 Controller 进行注册
        for (SpringMvcControllerExtension ext : extensions) {
            for (Class<?> controllerClass : ext.getControllerClasses()) {
                String beanName = PluginSpringBeanUtils.buildPluginBeanName(pluginId, controllerClass);

                // 2.1 注册前进行路径冲突检测
                try {
                    validateControllerMappingConflicts(controllerClass, handlerMapping, pluginId);
                } catch (IllegalStateException conflict) {
                    log.error("插件 {} 注册 Controller 失败，检测到路径冲突: class={}, msg={}", pluginId,
                        controllerClass.getName(), conflict.getMessage());
                    pluginManager.unloadPlugin(pluginId);
                    unregisterPluginControllers(pluginId);
                    return false;
                }

                // 2.2 真正注册 Controller Bean 及其 RequestMapping
                try {
                    Object controllerBean = acf.createBean(controllerClass);
                    PluginSpringBeanUtils.registerPluginBean(pluginId, beanName, controllerBean, applicationContext,
                        pluginBeanNames);

                    registerRequestMappingsForController(pluginId, handlerMapping, beanName, controllerClass, mappings);

                    beanNames.add(beanName);
                } catch (Exception e) {
                    log.error("插件 {} 注册 Controller 失败: class={}", pluginId, controllerClass.getName(), e);
                    pluginManager.unloadPlugin(pluginId);
                    unregisterPluginControllers(pluginId);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 为单个 Controller Bean 注册 RequestMapping，并记录映射信息
     */
    private void registerRequestMappingsForController(String pluginId, RequestMappingHandlerMapping handlerMapping,
        String beanName, Class<?> controllerClass, List<RequestMappingInfo> mappings) {
        // 注册 RequestMapping（detectHandlerMethods 是受保护方法，这里通过反射调用）
        invokeDetectHandlerMethods(handlerMapping, beanName);

        // 记录该 Bean 对应的所有 RequestMappingInfo，后续卸载时使用
        List<RequestMappingInfo> infos = findMappingsByBeanName(handlerMapping, beanName);
        mappings.addAll(infos);

        log.info("插件 {} 注册 Controller 成功: beanName={}, class={}, mappings={}", pluginId, beanName,
            controllerClass.getName(), infos.size());
    }

    /**
     * 根据插件 ID 取消注册其所有的 RequestMappingInfo
     */
    private void unregisterPluginRequestMappings(String pluginId, RequestMappingHandlerMapping handlerMapping) {
        // 注意：这里只根据插件 ID 把之前为该插件记录过的映射全部移除
        List<RequestMappingInfo> infos = pluginMappings.remove(pluginId);
        if (infos == null) {
            return;
        }
        for (RequestMappingInfo info : infos) {
            handlerMapping.unregisterMapping(info);
        }
        log.info("插件 {} 取消注册 RequestMapping 数量: {}", pluginId, infos.size());
    }

    /**
     * 销毁插件动态注册的所有 Bean（包括 Service、Component、Controller 等）
     */
    private void destroyPluginBeans(String pluginId, ConfigurableListableBeanFactory beanFactory) {
        // 先复制一份，便于销毁后移除 BeanDefinition（destroyPluginBeans 会移除 pluginBeanNames 记录）
        List<String> beanNames = pluginBeanNames.get(pluginId);
        List<String> copy = beanNames == null ? Collections.emptyList() : new ArrayList<>(beanNames);

        PluginSpringBeanUtils.destroyPluginBeans(pluginId, applicationContext, pluginBeanNames,
            beanName -> log.info("插件 {} 销毁插件 Bean: {}", pluginId, beanName),
            (beanName, ex) -> log.warn("插件 {} 销毁插件 Bean 失败(忽略继续): beanName={}", pluginId, beanName, ex));

        // 再移除 BeanDefinition，避免 BeanDefinition 持有插件 Class 引用导致类加载器无法回收
        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            for (String beanName : copy) {
                if (registry.containsBeanDefinition(beanName)) {
                    try {
                        registry.removeBeanDefinition(beanName);
                        log.info("插件 {} 移除插件 BeanDefinition: {}", pluginId, beanName);
                    } catch (Exception ex) {
                        log.warn("插件 {} 移除插件 BeanDefinition 失败(忽略继续): beanName={}", pluginId, beanName, ex);
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
    /**
     * 通过反射调用 RequestMappingHandlerMapping.detectHandlerMethods
     * <p>
     * 该方法在 Spring 中是 protected，不能直接从外部调用，只能通过反射或自定义子类暴露。
     */
    private void invokeDetectHandlerMethods(RequestMappingHandlerMapping handlerMapping, String beanName) {
        try {
            // 使用 Spring 提供的工具从类及其父类中查找方法，兼容不同版本
            java.lang.reflect.Method method = ReflectionUtils.findMethod(handlerMapping.getClass(),
                "detectHandlerMethods", Object.class);
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

    /**
     * 在真正注册 Controller 之前，基于 Spring 的 RequestMapping 规则预先检测路径冲突。
     * <p>
     * 规则简化为： 1. 若新 Controller 中某个方法的 URL pattern 与现有映射完全相同，且 HTTP 方法存在交集（或任一方未限制方法），视为冲突； 2. 一旦发现冲突，抛出
     * IllegalStateException，阻止插件继续注册。
     */
    private void validateControllerMappingConflicts(Class<?> controllerClass,
        RequestMappingHandlerMapping handlerMapping, String pluginId) {
        Map<RequestMappingInfo, HandlerMethod> existing = handlerMapping.getHandlerMethods();
        if (existing == null || existing.isEmpty()) {
            return;
        }

        // 通过反射调用 protected RequestMappingHandlerMapping.getMappingForMethod(Method, Class<?>)
        Method getMappingForMethod = ReflectionUtils.findMethod(handlerMapping.getClass(), "getMappingForMethod",
            Method.class, Class.class);
        if (getMappingForMethod == null) {
            log.warn("无法找到 RequestMappingHandlerMapping.getMappingForMethod(Method, Class)，跳过路径冲突预检测");
            return;
        }
        ReflectionUtils.makeAccessible(getMappingForMethod);

        Method[] methods = controllerClass.getMethods();
        for (Method method : methods) {
            RequestMappingInfo newInfo;
            try {
                newInfo = (RequestMappingInfo) getMappingForMethod.invoke(handlerMapping, method, controllerClass);
            } catch (Exception e) {
                log.warn("分析插件 {} Controller 映射失败, class={}, method={}", pluginId, controllerClass.getName(),
                    method.getName(), e);
                continue;
            }
            if (newInfo == null) {
                continue;
            }

            Set<String> newPatterns = extractPatterns(newInfo);
            Set<RequestMethod> newMethods = newInfo.getMethodsCondition().getMethods();

            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : existing.entrySet()) {
                RequestMappingInfo existInfo = entry.getKey();
                Set<String> existPatterns = extractPatterns(existInfo);
                Set<RequestMethod> existMethods = existInfo.getMethodsCondition().getMethods();

                if (hasPatternConflict(newPatterns, existPatterns) && hasMethodConflict(newMethods, existMethods)) {
                    String existBean = String.valueOf(entry.getValue().getBean());
                    String conflictPattern = firstCommonPattern(newPatterns, existPatterns);
                    String message = String.format(
                        "插件 Controller 路由冲突: plugin=%s, controller=%s, method=%s, url=%s, newHttpMethods=%s, existingBean=%s, existingHttpMethods=%s",
                        pluginId, controllerClass.getName(), method.getName(), conflictPattern, newMethods, existBean,
                        existMethods);
                    throw new PluginControllerMappingConflictException(message);
                }
            }
        }
    }

    /**
     * 统一从 RequestMappingInfo 中提取 URL pattern 字符串，兼容两种路径匹配策略：
     * <p>
     * - 传统 AntPathMatcher：使用 PatternsRequestCondition.getPatterns() <br> - PathPatternParser：使用
     * PathPatternsRequestCondition.getPatternValues()
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractPatterns(RequestMappingInfo info) {
        if (info == null) {
            return Collections.emptySet();
        }

        // 1. 优先尝试传统的 PatternsRequestCondition（Ant 风格路径）
        try {
            if (info.getPatternsCondition() != null) {
                Set<String> patterns = info.getPatternsCondition().getPatterns();
                if (patterns != null && !patterns.isEmpty()) {
                    return patterns;
                }
            }
        } catch (Exception ex) {
            // 理论上这里不应抛异常，如有异常仅记录调试日志并继续尝试 PathPattern 分支
            log.debug("从 PatternsRequestCondition 提取 URL pattern 失败，将尝试 PathPatternsRequestCondition, info={}",
                info, ex);
        }

        // 2. 兼容 Spring 5.3+ 引入的 PathPatternParser（Boot 2.6+ 默认开启）
        try {
            // 通过反射调用 getPathPatternsCondition，避免对特定 Spring 版本产生硬依赖
            Method method = RequestMappingInfo.class.getMethod("getPathPatternsCondition");
            Object pathPatternsCondition = method.invoke(info);
            if (pathPatternsCondition == null) {
                return Collections.emptySet();
            }

            // 优先使用 getPatternValues()（Set<String>），若不存在则回退到 getPatterns()（Set<?>）
            try {
                Method getPatternValues = pathPatternsCondition.getClass().getMethod("getPatternValues");
                Object values = getPatternValues.invoke(pathPatternsCondition);
                if (values instanceof Set) {
                    return (Set<String>) values;
                }
            } catch (NoSuchMethodException ignore) {
                // 忽略，继续尝试 getPatterns()
            }

            Method getPatterns = pathPatternsCondition.getClass().getMethod("getPatterns");
            Object patternsObj = getPatterns.invoke(pathPatternsCondition);
            if (patternsObj instanceof Set) {
                Set<?> raw = (Set<?>) patternsObj;
                Set<String> result = new LinkedHashSet<>();
                for (Object p : raw) {
                    if (p != null) {
                        result.add(p.toString());
                    }
                }
                return result;
            }
        } catch (NoSuchMethodException e) {
            // 旧版本 Spring 未引入 PathPattern 支持，直接忽略
            log.debug("当前 Spring 版本不支持 PathPatternsRequestCondition, info={}", info);
        } catch (Exception ex) {
            // 为避免影响主流程，这里只记录告警并返回空集合
            log.warn("从 PathPatternsRequestCondition 提取 URL pattern 失败，将忽略该映射进行冲突检测, info={}", info,
                ex);
        }

        return Collections.emptySet();
    }

    /**
     * 判断 URL pattern 是否存在完全相同的情况
     */
    private boolean hasPatternConflict(Set<String> p1, Set<String> p2) {
        if (p1 == null || p2 == null || p1.isEmpty() || p2.isEmpty()) {
            return false;
        }
        for (String a : p1) {
            for (String b : p2) {
                if (a != null && a.equals(b)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 取两个 pattern 集合中的任意一个相同值，便于构造错误提示信息。
     */
    private String firstCommonPattern(Set<String> p1, Set<String> p2) {
        if (p1 == null || p2 == null) {
            return null;
        }
        for (String a : p1) {
            if (a != null && p2.contains(a)) {
                return a;
            }
        }
        return null;
    }

    /**
     * 判断 HTTP 方法是否冲突： - 任一方未声明方法（methods 为空）视为匹配任意方法； - 否则只要有一个 RequestMethod 相同即可视为冲突。
     */
    private boolean hasMethodConflict(Set<RequestMethod> m1, Set<RequestMethod> m2) {
        // 任一方未指定 HTTP 方法，等价于“所有方法”，与另一方存在交集
        if (m1 == null || m1.isEmpty() || m2 == null || m2.isEmpty()) {
            return true;
        }
        for (RequestMethod a : m1) {
            if (m2.contains(a)) {
                return true;
            }
        }
        return false;
    }
}
