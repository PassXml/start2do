package org.start2do.plugin.api.spring;

import java.util.Collection;
import org.pf4j.ExtensionPoint;

/**
 * MyBatis 数据源扩展点
 * <p>
 * 插件通过实现该接口，声明自身需要的逻辑数据源及其基础配置，
 * 由宿主在运行时动态创建并管理这些 DataSource。
 */
public interface MybatisDataSourceExtension extends ExtensionPoint {

    /**
     * 返回插件需要注册的数据源列表
     */
    Collection<PluginDatabaseMeta> getDatabases();
}

