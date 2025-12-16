package org.start2do.plugin.mybatis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.start2do.MultiDatasourcePrimaryConfig;
import org.start2do.MybatisDatasourceFactory;
import org.start2do.plugin.api.spring.MybatisDataSourceExtension;
import org.start2do.plugin.api.spring.PluginDatabaseMeta;
import org.start2do.plugin.api.spring.PluginSpringBeanUtils;

/**
 * PF4J 与 MyBatis 数据源的桥接组件
 * <p>
 * 职责：
 * 1. 监听插件生命周期（启动 / 停止）
 * 2. 在插件启动时，根据插件声明的 PluginDatabaseMeta 动态创建 DataSource
 * 3. 将 DataSource 注册到 MybatisDataSourceRegistry，并可选注册为 Spring Bean
 * 4. 在插件停止 / 卸载时，关闭并移除对应 DataSource，避免连接与类加载器泄漏
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Pf4jMybatisDataSourceBridge implements PluginStateListener {

    private final PluginManager pluginManager;

    private final ConfigurableApplicationContext applicationContext;

    private final MybatisDataSourceRegistry dataSourceRegistry;

    /**
     * 记录每个插件动态注册的数据源相关 Bean 名称，便于卸载时集中销毁。
     */
    private final Map<String, List<String>> pluginBeanNames = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (pluginManager == null) {
            log.info("Pf4jMybatisDataSourceBridge 初始化: 未找到 PluginManager，跳过插件数据源动态注册");
            return;
        }

        pluginManager.addPluginStateListener(this);

        // 对已 STARTED 的插件做一次补偿注册
        for (PluginWrapper wrapper : pluginManager.getPlugins()) {
            if (wrapper.getPluginState() == PluginState.STARTED) {
                registerPluginDataSources(wrapper.getPluginId());
            }
        }

        log.info("Pf4jMybatisDataSourceBridge 初始化完成");
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        String pluginId = event.getPlugin().getPluginId();
        PluginState state = event.getPluginState();
        if (state == PluginState.STARTED) {
            registerPluginDataSources(pluginId);
        } else if (state == PluginState.UNLOADED
            || state == PluginState.STOPPED
            || state == PluginState.DISABLED
            || state == PluginState.FAILED) {
            unregisterPluginDataSources(pluginId);
        }
    }

    /**
     * 插件启动时，根据其 MybatisDataSourceExtension 声明创建 DataSource
     */
    private void registerPluginDataSources(String pluginId) {
        List<MybatisDataSourceExtension> exts =
            pluginManager.getExtensions(MybatisDataSourceExtension.class, pluginId);

        if (exts.isEmpty()) {
            log.info("插件 {} 未实现 MybatisDataSourceExtension，跳过插件数据源注册", pluginId);
            return;
        }

        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

        for (MybatisDataSourceExtension ext : exts) {
            for (PluginDatabaseMeta meta : ext.getDatabases()) {
                if (meta == null || meta.getId() == null || meta.getId().trim().isEmpty()) {
                    continue;
                }
                String dataSourceId = meta.getId().trim();
                try {
                    // 构造多数据源配置，与现有 MybatisDatasourceFactory 保持一致
                    MultiDatasourcePrimaryConfig cfg = new MultiDatasourcePrimaryConfig()
                        .setDateType(meta.getDataType())
                        .setDriverClassName(meta.getDriverClassName())
                        .setUrl(meta.getUrl())
                        .setUsername(meta.getUsername())
                        .setPassword(meta.getPassword())
                        .setType((Class) meta.getDataSourceType());

                    DataSource ds = MybatisDatasourceFactory.dataSource(cfg);

                    // 注册到数据源注册中心
                    dataSourceRegistry.registerPluginDataSource(pluginId, dataSourceId, ds);

                    // 同时注册为 Spring Bean，方便宿主或插件通过 beanName 注入使用（可选）
                    String beanName = buildDataSourceBeanName(pluginId, dataSourceId);
                    PluginSpringBeanUtils.registerPluginBean(pluginId, beanName, ds, beanFactory, pluginBeanNames);
                    log.info("插件 {} 注册 MyBatis 数据源成功: id={}, beanName={}", pluginId, dataSourceId, beanName);
                } catch (Exception e) {
                    log.error("插件 {} 注册 MyBatis 数据源失败: id={}", pluginId, meta.getId(), e);
                }
            }
        }

        // pluginBeanNames 的维护已在 PluginSpringBeanUtils.registerPluginBean 中统一处理
    }

    /**
     * 插件停止/卸载时，清理其数据源 Bean 并通知注册中心关闭数据源
     */
    private void unregisterPluginDataSources(String pluginId) {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        PluginSpringBeanUtils.destroyPluginBeans(
            pluginId,
            beanFactory,
            pluginBeanNames,
            beanName -> log.info("插件 {} 销毁 MyBatis 数据源 Bean: {}", pluginId, beanName),
            (beanName, ex) -> log
                .warn("插件 {} 销毁 MyBatis 数据源 Bean 失败(忽略继续): {}", pluginId, beanName, ex)
        );

        // 通知注册中心关闭并移除插件数据源
        dataSourceRegistry.removePluginDataSources(pluginId);
    }

    /**
     * 构造插件数据源在 Spring 容器中的 Bean 名称
     */
    private String buildDataSourceBeanName(String pluginId, String dataSourceId) {
        return "plugin_" + pluginId + "_" + dataSourceId + "_dataSource";
    }
}
