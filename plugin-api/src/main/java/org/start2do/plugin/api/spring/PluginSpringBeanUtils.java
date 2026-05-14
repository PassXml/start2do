package org.start2do.plugin.api.spring;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 插件在宿主 Spring 容器中动态注册 / 销毁 Bean 的通用工具类。
 * <p>
 * 设计目标：
 * <ul>
 *   <li>统一插件 Bean 命名规范；</li>
 *   <li>封装 destroySingleton / removeSingleton 的反射细节，避免各 Bridge 模块重复实现；</li>
 *   <li>由调用方注入日志函数，避免在 API 模块中直接依赖具体日志框架。</li>
 * </ul>
 */
@Slf4j
@UtilityClass
public class PluginSpringBeanUtils {

    private static final String PLUGIN_EVENT_LISTENER_BEAN_NAME_INFIX = "_eventListener_";
    private static final String EVENT_EXPRESSION_EVALUATOR_CLASS_NAME =
        "org.springframework.context.event.EventExpressionEvaluator";

    private static volatile Method applicationListenerMethodAdapterInitMethod;
    private static volatile Object eventExpressionEvaluator;

    /**
     * 构造插件 Bean 在 Spring 容器中的名称。
     * <p>
     * 统一命名约定： plugin_{pluginId}_{beanClassFqn}
     *
     * @param pluginId  插件 ID
     * @param beanClass Bean 类型
     * @return 规范化的 Bean 名称
     */
    public String buildPluginBeanName(String pluginId, Class<?> beanClass) {
        return "plugin_" + pluginId + "_" + beanClass.getName();
    }

    /**
     * 为动态注册的 BeanDefinition 显式补齐 JSR-250 生命周期方法。
     * <p>
     * 背景：插件 Bean 通过运行期注册 BeanDefinition 进入容器，显式写入 init/destroy 元数据后，
     * 可以避免不同注册路径或后处理器时机差异导致的 {@link PostConstruct}/{@link PreDestroy} 漏执行。
     */
    public void applyLifecycleMetadata(RootBeanDefinition beanDefinition, Class<?> beanClass) {
        String initMethodName = findLifecycleMethodName(beanClass, PostConstruct.class);
        if (initMethodName != null) {
            beanDefinition.setInitMethodName(initMethodName);
        }

        String destroyMethodName = findLifecycleMethodName(beanClass, PreDestroy.class);
        if (destroyMethodName != null) {
            beanDefinition.setDestroyMethodName(destroyMethodName);
        }
    }

    /**
     * 为插件注册一个单例 Bean，并记录到插件级别的 Bean 名称缓存中。
     *
     * @param pluginId        插件 ID
     * @param beanName        Bean 名称
     * @param bean            Bean 实例
     * @param beanFactory     Spring BeanFactory
     * @param pluginBeanNames 插件 -> Bean 名称列表映射
     */
    public void registerPluginBean(String pluginId,
        String beanName,
        Object bean,
        ConfigurableListableBeanFactory beanFactory,
        Map<String, List<String>> pluginBeanNames) {

        beanFactory.registerSingleton(beanName, bean);
        pluginBeanNames.computeIfAbsent(pluginId, k -> new ArrayList<String>()).add(beanName);
    }

    /**
     * 为插件注册一个单例 Bean（并额外补齐动态 @EventListener 的监听器注册）。
     * <p>
     * 背景：Spring 的 {@link EventListener} 方法通常在容器 refresh 阶段由 {@code EventListenerMethodProcessor} 扫描并注册为监听器；插件在运行期动态注册
     * Bean 时，该扫描不会再次触发，导致后注册的 @EventListener 不生效。
     *
     * @param pluginId           插件 ID
     * @param beanName           Bean 名称
     * @param bean               Bean 实例
     * @param applicationContext Spring ApplicationContext
     * @param pluginBeanNames    插件 -> Bean 名称列表映射
     */
    public void registerPluginBean(String pluginId,
        String beanName,
        Object bean,
        ConfigurableApplicationContext applicationContext,
        Map<String, List<String>> pluginBeanNames) {

        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        registerPluginBean(pluginId, beanName, bean, beanFactory, pluginBeanNames);
        registerEventListenerAdaptersIfNecessary(pluginId, beanName, bean, applicationContext, pluginBeanNames);
    }

    /**
     * 为“已存在于容器中的插件 Bean”补齐动态 {@link EventListener} 的监听器注册。
     * <p>
     * 典型场景：插件使用 BeanDefinitionRegistry 动态注册 BeanDefinition 并由 Spring 负责实例化，
     * 此时不会走 {@link #registerPluginBean(String, String, Object, ConfigurableApplicationContext, Map)}，
     * 需要显式调用本方法以保证 @EventListener 生效。
     */
    public void registerEventListenerAdaptersForExistingBeanIfNecessary(String pluginId,
        String sourceBeanName,
        Object sourceBean,
        ConfigurableApplicationContext applicationContext,
        Map<String, List<String>> pluginBeanNames) {
        registerEventListenerAdaptersIfNecessary(pluginId, sourceBeanName, sourceBean, applicationContext,
            pluginBeanNames);
    }

    private void registerEventListenerAdaptersIfNecessary(String pluginId,
        String sourceBeanName,
        Object sourceBean,
        ConfigurableApplicationContext applicationContext,
        Map<String, List<String>> pluginBeanNames) {

        Class<?> userClass = ClassUtils.getUserClass(sourceBean);
        List<Method> eventListenerMethods = new ArrayList<>();
        ReflectionUtils.doWithMethods(userClass,
            method -> {
                if (AnnotatedElementUtils.hasAnnotation(method, EventListener.class)) {
                    eventListenerMethods.add(method);
                }
            });

        if (eventListenerMethods.isEmpty()) {
            return;
        }

        log.info("插件 {} 检测到 @EventListener: sourceBeanName={}, class={}, methods={}", pluginId, sourceBeanName,
            userClass.getName(), eventListenerMethods.size());

        ApplicationEventMulticaster multicaster;
        try {
            multicaster = applicationContext.getBean(ApplicationEventMulticaster.class);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            // 极端情况：容器中没有 multicaster，无法保证动态监听器注册可生效；此时选择静默跳过，避免影响插件其它能力。
            return;
        }

        for (Method method : eventListenerMethods) {
            String listenerBeanName = buildPluginEventListenerBeanName(pluginId, sourceBeanName, method);
            if (applicationContext.containsBean(listenerBeanName)) {
                continue;
            }
            ApplicationListenerMethodAdapter adapter =
                new ApplicationListenerMethodAdapter(sourceBeanName, userClass, method);
            if (!initApplicationListenerMethodAdapter(adapter, applicationContext)) {
                log.error("插件 {} 初始化 @EventListener 适配器失败: sourceBeanName={}, listenerBeanName={}, method={}",
                    pluginId, sourceBeanName, listenerBeanName, method.toGenericString());
                continue;
            }
            Object initialized = applicationContext.getAutowireCapableBeanFactory()
                .initializeBean(adapter, listenerBeanName);

            applicationContext.getBeanFactory().registerSingleton(listenerBeanName, initialized);
            pluginBeanNames.computeIfAbsent(pluginId, k -> new ArrayList<String>()).add(listenerBeanName);
            multicaster.addApplicationListenerBean(listenerBeanName);
            log.info("插件 {} 注册 @EventListener 适配器: listenerBeanName={}, method={}", pluginId, listenerBeanName,
                method.getName());
        }
    }

    /**
     * Spring 5.3.x 中 {@link ApplicationListenerMethodAdapter} 的 {@code init(ApplicationContext, EventExpressionEvaluator)}
     * 为包级可见；若不调用，适配器内部 {@code applicationContext} 将为 null，事件触发时会抛出
     * {@link IllegalArgumentException}（ApplicationContext must not be null）。
     */
    private boolean initApplicationListenerMethodAdapter(ApplicationListenerMethodAdapter adapter,
        ConfigurableApplicationContext applicationContext) {
        try {
            Method initMethod = applicationListenerMethodAdapterInitMethod;
            Object evaluator = eventExpressionEvaluator;

            if (initMethod == null || evaluator == null) {
                synchronized (PluginSpringBeanUtils.class) {
                    initMethod = applicationListenerMethodAdapterInitMethod;
                    evaluator = eventExpressionEvaluator;
                    if (initMethod == null || evaluator == null) {
                        ClassLoader classLoader = ApplicationListenerMethodAdapter.class.getClassLoader();
                        Class<?> evaluatorClass = Class.forName(EVENT_EXPRESSION_EVALUATOR_CLASS_NAME, true,
                            classLoader);
                        Constructor<?> ctor = evaluatorClass.getDeclaredConstructor();
                        ReflectionUtils.makeAccessible(ctor);
                        evaluator = ctor.newInstance();
                        Method candidate = ReflectionUtils.findMethod(ApplicationListenerMethodAdapter.class, "init",
                            ApplicationContext.class, evaluatorClass);
                        if (candidate == null) {
                            return false;
                        }
                        ReflectionUtils.makeAccessible(candidate);
                        applicationListenerMethodAdapterInitMethod = candidate;
                        eventExpressionEvaluator = evaluator;
                        initMethod = candidate;
                    }
                }
            }

            if (!Modifier.isPublic(initMethod.getModifiers())) {
                ReflectionUtils.makeAccessible(initMethod);
            }
            initMethod.invoke(adapter, applicationContext, evaluator);
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    private String buildPluginEventListenerBeanName(String pluginId, String sourceBeanName, Method method) {
        String key = sourceBeanName + "#" + method.toGenericString();
        return "plugin_" + pluginId + PLUGIN_EVENT_LISTENER_BEAN_NAME_INFIX + Integer.toHexString(key.hashCode());
    }

    private String findLifecycleMethodName(Class<?> beanClass, Class<? extends Annotation> annotationClass) {
        Class<?> userClass = ClassUtils.getUserClass(beanClass);
        final Method[] found = new Method[1];
        ReflectionUtils.doWithMethods(userClass, method -> {
            if (found[0] != null) {
                return;
            }
            if (AnnotatedElementUtils.hasAnnotation(method, annotationClass)
                && method.getParameterCount() == 0
                && !Modifier.isStatic(method.getModifiers())) {
                found[0] = method;
            }
        });
        return found[0] == null ? null : found[0].getName();
    }

    /**
     * 销毁指定插件动态注册的所有 Bean。
     * <p>
     * 销毁顺序：
     * <ol>
     *   <li>优先尝试调用 ConfigurableListableBeanFactory.destroySingleton(String)；</li>
     *   <li>若方法不存在，则按常规方式销毁 Bean，并尝试通过 removeSingleton(String) 将其从单例缓存中移除。</li>
     * </ol>
     *
     * @param pluginId        插件 ID
     * @param beanFactory     Spring BeanFactory
     * @param pluginBeanNames 插件 -> Bean 名称列表映射（方法内部会移除对应插件的记录）
     * @param infoLogger      信息日志回调，形如 msg -> {}，可为 null
     * @param warnLogger      告警日志回调，形如 (msg, ex) -> {}，可为 null
     */
    public void destroyPluginBeans(String pluginId,
        ConfigurableListableBeanFactory beanFactory,
        Map<String, List<String>> pluginBeanNames,
        Consumer<String> infoLogger,
        BiConsumer<String, Exception> warnLogger) {

        List<String> beanNames = pluginBeanNames.remove(pluginId);
        if (beanNames == null || beanNames.isEmpty()) {
            return;
        }

        for (String beanName : beanNames) {
            if (!beanFactory.containsSingleton(beanName)) {
                continue;
            }
            try {
                // 优先尝试 destroySingleton(String)
                Method destroySingleton =
                    ReflectionUtils.findMethod(beanFactory.getClass(), "destroySingleton", String.class);
                if (destroySingleton != null) {
                    ReflectionUtils.makeAccessible(destroySingleton);
                    destroySingleton.invoke(beanFactory, beanName);
                } else {
                    // 回退方案：按常规方式销毁 Bean，再尝试 removeSingleton(String)
                    Object bean = beanFactory.getBean(beanName);
                    beanFactory.destroyBean(bean);

                    Method removeSingleton =
                        ReflectionUtils.findMethod(beanFactory.getClass(), "removeSingleton", String.class);
                    if (removeSingleton != null) {
                        ReflectionUtils.makeAccessible(removeSingleton);
                        removeSingleton.invoke(beanFactory, beanName);
                    }
                }
                if (infoLogger != null) {
                    infoLogger.accept(beanName);
                }
            } catch (Exception ex) {
                if (warnLogger != null) {
                    warnLogger.accept(beanName, ex);
                }
            }
        }
    }

    /**
     * 销毁指定插件动态注册的所有 Bean（并同步清理通过
     * {@link #registerPluginBean(String, String, Object, ConfigurableApplicationContext, Map)} 注册的动态事件监听器）。
     *
     * @param pluginId           插件 ID
     * @param applicationContext Spring ApplicationContext
     * @param pluginBeanNames    插件 -> Bean 名称列表映射（方法内部会移除对应插件的记录）
     * @param infoLogger         信息日志回调，可为 null
     * @param warnLogger         告警日志回调，可为 null
     */
    public void destroyPluginBeans(String pluginId,
        ConfigurableApplicationContext applicationContext,
        Map<String, List<String>> pluginBeanNames,
        Consumer<String> infoLogger,
        BiConsumer<String, Exception> warnLogger) {

        List<String> beanNames = pluginBeanNames.get(pluginId);
        if (beanNames == null || beanNames.isEmpty()) {
            return;
        }

        ApplicationEventMulticaster multicaster = null;
        try {
            multicaster = applicationContext.getBean(ApplicationEventMulticaster.class);
        } catch (Exception ignored) {
            // ignore
        }

        if (multicaster != null) {
            // 先解除监听器引用，避免 multicaster 持有 beanName 导致插件类加载器无法回收
            for (String beanName : new ArrayList<>(beanNames)) {
                try {
                    multicaster.removeApplicationListenerBean(beanName);
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }

        destroyPluginBeans(pluginId, applicationContext.getBeanFactory(), pluginBeanNames, infoLogger, warnLogger);
    }
}
