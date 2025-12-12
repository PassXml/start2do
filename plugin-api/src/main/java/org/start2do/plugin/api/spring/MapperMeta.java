package org.start2do.plugin.api.spring;

/**
 * 插件声明的 Mapper 元信息
 * <p>
 * 用于在运行时将指定 Mapper 绑定到某个逻辑数据源 ID 上，由宿主据此创建 Mapper 代理。
 */
public final class MapperMeta {

    /**
     * 逻辑数据源 ID，对应 PluginDatabaseMeta.id 或宿主预先注册的数据源 ID。
     */
    private final String dataSourceId;

    /**
     * Mapper 接口类型
     */
    private final Class<?> mapperClass;

    /**
     * 默认逻辑数据源 ID，约定为宿主 primary 数据源
     */
    public static final String DEFAULT_DATASOURCE_ID = "primary";

    public MapperMeta(String dataSourceId, Class<?> mapperClass) {
        this.dataSourceId = dataSourceId;
        this.mapperClass = mapperClass;
    }

    /**
     * 使用默认数据源 ID 构造 MapperMeta
     * <p>
     * 默认等价于使用宿主配置的 primary 数据源：
     * plugin.mybatis.host-datasources.id = "primary"
     */
    public MapperMeta(Class<?> mapperClass) {
        this(DEFAULT_DATASOURCE_ID, mapperClass);
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public Class<?> getMapperClass() {
        return mapperClass;
    }
}
