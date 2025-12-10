package org.start2do.plugin.api.spring;

import java.util.Collection;
import org.pf4j.ExtensionPoint;

/**
 * Spring MVC 控制器扩展点
 * <p>
 * 插件通过实现该接口，将需要暴露给宿主应用的 Controller 类声明出来，
 * 由宿主在运行时将这些类动态注册为 Spring MVC 控制器。
 */
public interface SpringMvcControllerExtension extends ExtensionPoint {

    /**
     * 返回需要注册为 Spring MVC 控制器的类列表
     *
     * @return Controller 类集合
     */
    Collection<Class<?>> getControllerClasses();
}

