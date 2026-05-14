package org.start2do.plugin.config;

import java.nio.file.Paths;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.start2do.plugin.api.event.IDomainEventStore;
import org.start2do.plugin.manager.PropertiesOnlyJarPluginManager;

/**
 * plugin-service 自动配置
 * <p>
 * 负责提供 PF4J PluginManager 与默认的 IDomainEventStore， 并启用统一的 PluginSystemProperties 配置。
 */
@Configuration
@ComponentScan("org.start2do.plugin")
@EnableConfigurationProperties(PluginSystemProperties.class)
public class PluginAutoPluginAutoConfiguration {

    /**
     * PF4J 插件管理器，使用 plugin.runtime.storage-path 作为插件目录
     */
    @Bean
    public PluginManager pluginManager(PluginSystemProperties configuration,
        AutowireCapableBeanFactory beanFactory) {
        return new PropertiesOnlyJarPluginManager(Paths.get(configuration.getRuntime().getStoragePath()),
            beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean(IDomainEventStore.class)
    public IDomainEventStore domainEventStore() {
        return new IDomainEventStore() {
        };
    }
}
