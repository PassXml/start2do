package org.start2do.plugin.api.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明 PF4J 插件的基本元数据。
 * <p>
 * 编译期注解处理器会根据该注解生成 plugin.properties 文件，
 * 由 PF4J 在运行时自动读取插件描述信息。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginDescriptor {

    /**
     * 插件唯一标识，对应 PF4J 的 plugin.id。
     */
    String id();

    /**
     * 插件版本号，对应 PF4J 的 plugin.version。
     */
    String version();

    /**
     * 插件提供方（作者 / 组织），对应 PF4J 的 plugin.provider。
     */
    String provider() default "";

    /**
     * 插件描述信息，对应 PF4J 的 plugin.description。
     */
    String description() default "";
}

