package org.start2do.plugin.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginRuntimeException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * 让 PF4J 扩展实例补齐 Spring 自动注入、BeanPostProcessor 与销毁回调。
 */
public class SpringLifecycleExtensionFactory implements ExtensionFactory {

    private final AutowireCapableBeanFactory beanFactory;
    private final Map<ClassLoader, Map<String, Object>> cache = new ConcurrentHashMap<>();

    public SpringLifecycleExtensionFactory(PluginManager pluginManager, AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        pluginManager.addPluginStateListener(event -> {
            if (!event.getPluginState().isStarted()) {
                destroyExtensions(event.getPlugin().getPluginClassLoader());
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> extensionClass) {
        ClassLoader classLoader = extensionClass.getClassLoader();
        Map<String, Object> bucket = cache.computeIfAbsent(classLoader, key -> new ConcurrentHashMap<>());
        return (T) bucket.computeIfAbsent(extensionClass.getName(),
            key -> createAndInitialize(extensionClass, buildBeanName(extensionClass)));
    }

    private <T> Object createAndInitialize(Class<T> extensionClass, String beanName) {
        ClassLoader originalTccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(extensionClass.getClassLoader());
            T instance = extensionClass.getDeclaredConstructor().newInstance();
            beanFactory.autowireBean(instance);
            return beanFactory.initializeBean(instance, beanName);
        } catch (Exception ex) {
            throw new PluginRuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(originalTccl);
        }
    }

    private void destroyExtensions(ClassLoader classLoader) {
        Map<String, Object> bucket = cache.remove(classLoader);
        if (bucket == null || bucket.isEmpty()) {
            return;
        }
        for (Object extension : bucket.values()) {
            beanFactory.destroyBean(extension);
        }
    }

    private String buildBeanName(Class<?> extensionClass) {
        return "pf4jExtension:" + extensionClass.getName();
    }
}
