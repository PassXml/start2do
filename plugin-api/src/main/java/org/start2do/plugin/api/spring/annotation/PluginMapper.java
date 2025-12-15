package org.start2do.plugin.api.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.start2do.plugin.api.spring.MapperMeta;

/**
 * 标记插件中的 MyBatis Mapper 接口，并声明其绑定的数据源 ID。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginMapper {

    /**
     * 逻辑数据源 ID，默认为 {@link MapperMeta#DEFAULT_DATASOURCE_ID}。
     */
    String dataSourceId() default MapperMeta.DEFAULT_DATASOURCE_ID;
}

