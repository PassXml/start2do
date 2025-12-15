package org.start2do.plugin.api.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记插件中的 Spring MVC 控制器类，由编译期处理器收集并生成
 * {@link org.start2do.plugin.api.spring.SpringMvcControllerExtension} 实现。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginController {
}

