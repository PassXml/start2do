package org.start2do.plugin.api.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link PluginDatabase} 的容器注解，用于支持在同一个类上声明多个数据源。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginDatabases {

    PluginDatabase[] value();
}

