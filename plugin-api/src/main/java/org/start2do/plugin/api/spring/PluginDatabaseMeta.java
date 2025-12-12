package org.start2do.plugin.api.spring;

import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 插件声明的数据源元信息
 * <p>
 * 仅用于描述数据源的基础配置以及插件 Mapper 的扫描位置， 由宿主在运行时据此创建 DataSource 等对象。
 */
@Setter
@Getter
@Accessors(chain = true)
public final class PluginDatabaseMeta {

    /**
     * 逻辑数据源 ID，在插件内部以及宿主注册表中唯一标识一个数据源。
     * <p>
     * 建议使用「插件前缀_用途」的形式，例如：order_main、report_readonly 等。
     */
    private  String id;

    /**
     * JDBC 连接配置
     */
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;

    /**
     * DataSource 实现类型，例如 com.zaxxer.hikari.HikariDataSource
     */
    private final Class<? extends DataSource> dataSourceType;

    /**
     * Mapper XML 文件扫描路径模式（可选），例如：
     * <pre>classpath*:mybatis/pluginA/*.xml</pre>
     * <p>
     * 当前版本的桥接实现主要通过注解 Mapper 工作， 该字段预留给后续需要基于 XML 的插件 Mapper 扩展。
     */
    private final String mapperLocationPattern;

    /**
     * 可选数据类型标识，用于与已有多数据源工厂的 dataType 概念对齐。
     */
    private final String dataType;


    public PluginDatabaseMeta(String id,
        String url,
        String username,
        String password,
        String driverClassName,
        Class<? extends DataSource> dataSourceType,
        String mapperLocationPattern,
        String dataType) {
        this.id = id;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.dataSourceType = dataSourceType;
        this.mapperLocationPattern = mapperLocationPattern;
        this.dataType = dataType;
    }

}

