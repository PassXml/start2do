package org.start2do.plugin.api.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记插件中的 SOAP WebService 实现类，并可选覆盖元数据。
 * <p>
 * 元数据为空字符串时，桥接逻辑会退回到 {@code @WebService} 注解或默认规则。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginSoapService {

    /**
     * 发布地址（相对于 CXF 基础路径），例如："/ws/hello"
     */
    String address() default "";

    /**
     * JAX-WS serviceName
     */
    String serviceName() default "";

    /**
     * JAX-WS portName
     */
    String portName() default "";

    /**
     * JAX-WS targetNamespace
     */
    String targetNamespace() default "";
}

