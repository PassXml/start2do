package org.start2do.plugin.api.spring;

import java.util.Collection;
import org.pf4j.ExtensionPoint;

/**
 * Spring 普通 Bean 扩展点（Service / Component 等）
 * <p>
 * 插件通过实现该接口，将需要注册到宿主 Spring 容器的 Bean 类声明出来，
 * 由宿主在运行时动态创建并注册这些 Bean。
 */
public interface SpringPluginBeansExtension extends ExtensionPoint {

    /**
     * 返回需要注册为 Spring Bean 的类列表
     *
     * @return Bean 类集合
     */
    Collection<Class<?>> getBeanClasses();
}

