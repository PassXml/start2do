package org.start2do.plugin.config;

import java.nio.file.Paths;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.start2do.plugin.api.event.IDomainEventStore;

@Configuration
public class PluginAutoPluginConfiguration {

    @Bean
    public PluginManager pluginManager(PluginStorageProperties configuration) {
        return new JarPluginManager(Paths.get(configuration.getStoragePath()));
    }

    @Bean
    @ConditionalOnMissingBean(IDomainEventStore.class)
    public IDomainEventStore domainEventStore() {
        return new IDomainEventStore() {
        };
    }
}
