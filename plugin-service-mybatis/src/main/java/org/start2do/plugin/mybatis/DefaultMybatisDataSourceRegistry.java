package org.start2do.plugin.mybatis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MybatisDataSourceRegistry 默认实现
 */
@Slf4j
@Component
public class DefaultMybatisDataSourceRegistry implements MybatisDataSourceRegistry {

    /**
     * 宿主侧数据源：dataSourceId -> DataSource
     */
    private final Map<String, DataSource> hostDataSources = new ConcurrentHashMap<>();

    /**
     * 插件侧数据源：pluginId -> (dataSourceId -> DataSource)
     */
    private final Map<String, Map<String, DataSource>> pluginDataSources = new ConcurrentHashMap<>();

    @Override
    public void registerHostDataSource(String dataSourceId, DataSource dataSource) {
        if (dataSourceId == null || dataSource == null) {
            return;
        }
        hostDataSources.put(dataSourceId, dataSource);
        log.info("MyBatis 注册宿主数据源: id={}", dataSourceId);
    }

    @Override
    public void registerPluginDataSource(String pluginId, String dataSourceId, DataSource dataSource) {
        if (pluginId == null || dataSourceId == null || dataSource == null) {
            return;
        }
        pluginDataSources
            .computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>())
            .put(dataSourceId, dataSource);
        log.info("MyBatis 注册插件数据源: pluginId={}, id={}", pluginId, dataSourceId);
    }

    @Override
    public DataSource resolveDataSource(String pluginId, String dataSourceId) {
        if (dataSourceId == null) {
            return null;
        }
        // 1. 先查插件自身的数据源
        if (pluginId != null) {
            Map<String, DataSource> map = pluginDataSources.get(pluginId);
            if (map != null) {
                DataSource ds = map.get(dataSourceId);
                if (ds != null) {
                    return ds;
                }
            }
        }
        // 2. 回退到宿主数据源
        return hostDataSources.get(dataSourceId);
    }

    @Override
    public void removePluginDataSources(String pluginId) {
        Map<String, DataSource> removed = pluginDataSources.remove(pluginId);
        if (removed == null || removed.isEmpty()) {
            return;
        }
        // 尝试关闭插件数据源，避免连接泄漏
        for (Map.Entry<String, DataSource> entry : removed.entrySet()) {
            String id = entry.getKey();
            DataSource ds = entry.getValue();
            try {
                // 优先尝试 AutoCloseable.close()
                if (ds instanceof AutoCloseable) {
                    ((AutoCloseable) ds).close();
                } else {
                    // 退而求其次，反射调用 close() 方法（例如 HikariDataSource）
                    try {
                        java.lang.reflect.Method closeMethod = ds.getClass().getMethod("close");
                        closeMethod.invoke(ds);
                    } catch (NoSuchMethodException ignore) {
                        // 不支持显式关闭的数据源，交由 GC 即可
                    }
                }
                log.info("MyBatis 关闭并移除插件数据源成功: pluginId={}, id={}", pluginId, id);
            } catch (Exception e) {
                log.warn("MyBatis 关闭插件数据源失败(忽略继续): pluginId={}, id={}", pluginId, id, e);
            }
        }
    }
}

