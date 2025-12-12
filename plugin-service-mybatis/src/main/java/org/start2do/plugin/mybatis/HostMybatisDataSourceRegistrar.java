package org.start2do.plugin.mybatis;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 宿主侧 MyBatis 数据源注册器
 * <p>
 * 启动时根据 PluginMybatisProperties，从 Spring 容器中获取指定的 SqlSessionTemplate，
 * 并将其底层 DataSource 注册到 MybatisDataSourceRegistry 中，供插件按 dataSourceId 复用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HostMybatisDataSourceRegistrar {

    private final ApplicationContext applicationContext;

    private final MybatisDataSourceRegistry dataSourceRegistry;

    private final PluginMybatisProperties properties;

    @PostConstruct
    public void registerHostDatasources() {
        if (properties.getHostDatasources() == null || properties.getHostDatasources().isEmpty()) {
            log.info("HostMybatisDataSourceRegistrar: 未配置 plugin.mybatis.host-datasources，跳过宿主数据源注册");
            return;
        }
        for (PluginMybatisProperties.HostDatasourceConfig cfg : properties.getHostDatasources()) {
            if (cfg.getId() == null || cfg.getId().trim().isEmpty()
                || cfg.getSqlSessionTemplateBean() == null || cfg.getSqlSessionTemplateBean().trim().isEmpty()) {
                continue;
            }
            String id = cfg.getId().trim();
            String beanName = cfg.getSqlSessionTemplateBean().trim();
            try {
                SqlSessionTemplate template =
                    applicationContext.getBean(beanName, SqlSessionTemplate.class);
                DataSource ds = template.getSqlSessionFactory().getConfiguration().getEnvironment().getDataSource();
                if (ds == null) {
                    log.warn("HostMybatisDataSourceRegistrar: SqlSessionTemplate Bean 无 DataSource，跳过: id={}, bean={}",
                        id, beanName);
                    continue;
                }
                dataSourceRegistry.registerHostDataSource(id, ds);
                log.info("HostMybatisDataSourceRegistrar: 注册宿主数据源成功: id={}, bean={}", id, beanName);
            } catch (Exception e) {
                log.warn("HostMybatisDataSourceRegistrar: 注册宿主数据源失败: id={}, bean={}", id, beanName, e);
            }
        }
    }
}

