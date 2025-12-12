package org.start2do.plugin.mybatis;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis 数据源注册中心
 * <p>
 * 职责：
 * 1. 统一管理宿主与插件声明的逻辑数据源（dataSourceId）
 * 2. 为插件 Mapper 提供按 pluginId + dataSourceId 解析 DataSource 的能力
 * 3. 插件卸载时清理其对应的 DataSource，避免资源与类加载器泄漏
 */
public interface MybatisDataSourceRegistry {

    /**
     * 注册宿主侧数据源
     *
     * @param dataSourceId 逻辑数据源 ID
     * @param dataSource   宿主提供的 DataSource 实例
     */
    void registerHostDataSource(String dataSourceId, DataSource dataSource);

    /**
     * 注册插件侧数据源
     *
     * @param pluginId     插件 ID
     * @param dataSourceId 逻辑数据源 ID
     * @param dataSource   插件基于 PluginDatabaseMeta 创建的 DataSource
     */
    void registerPluginDataSource(String pluginId, String dataSourceId, DataSource dataSource);

    /**
     * 解析插件使用的数据源
     * <p>
     * 解析顺序：
     * 1. 先查找插件自身声明的 dataSourceId
     * 2. 若不存在，则回退到宿主的同名 dataSourceId
     *
     * @return 匹配到的 DataSource，找不到则返回 null
     */
    DataSource resolveDataSource(String pluginId, String dataSourceId);

    /**
     * 插件卸载时移除其所有数据源
     *
     * @param pluginId 插件 ID
     */
    void removePluginDataSources(String pluginId);
}

