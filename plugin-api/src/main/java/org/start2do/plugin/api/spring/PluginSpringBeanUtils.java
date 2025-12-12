package org.start2do.plugin.api.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
@UtilityClass
public class PluginSpringBeanUtils {

    /**
     * 构造插件 Bean 在 Spring 容器中的名称。
     * <p>
     * 统一命名约定：
     * plugin_{pluginId}_{beanClassFqn}
     *
     * @param pluginId  插件 ID
     * @param beanClass Bean 类型
     * @return 规范化的 Bean 名称
     */
    public String buildPluginBeanName(String pluginId, Class<?> beanClass) {
        return "plugin_" + pluginId + "_" + beanClass.getName();
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
}

