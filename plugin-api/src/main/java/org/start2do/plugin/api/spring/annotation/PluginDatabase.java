package org.start2do.plugin.api.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记插件声明的 MyBatis 数据源配置。
 * <p>
 * 这些元数据将在编译期被处理器收集，并生成 {@code MybatisDataSourceExtension#getDatabases()} 的实现。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PluginDatabases.class)
public @interface PluginDatabase {

    /**
     * 逻辑数据源 ID，在插件内部唯一标识一个数据源。
     */
    String id();

    /**
     * JDBC URL。
     */
    String url();

    /**
     * JDBC 用户名。
     */
    String username();

    /**
     * JDBC 密码。
     */
    String password();

    /**
     * JDBC 驱动类名，例如 "com.mysql.jdbc.Driver"。
     */
    String driverClassName();

    /**
     * DataSource 实现类型，例如 com.zaxxer.hikari.HikariDataSource.class。
     */
    Class<?> dataSourceType();

    /**
     * Mapper XML 扫描路径模式（可选），为空字符串时等价于未配置。
     */
    String mapperLocationPattern() default "";

    /**
     * 可选数据类型标识，例如 "mysql" 等，便于与现有多数据源体系对齐。
     */
    String dataType() default "";
}
