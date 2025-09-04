package org.start2do.cep.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.start2do.cep.FlinkCEPProperties;
import org.start2do.cep.FlinkCEPTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

@AutoConfiguration
@ConditionalOnClass(FlinkCEPTool.class)
@EnableConfigurationProperties(FlinkCEPProperties.class)
@ConditionalOnProperty(prefix = "flink", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
        FlinkCEPTool.class
})
public class FlinkCEPAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlinkCEPTool flinkCEPTool(FlinkCEPProperties properties) {
        return new FlinkCEPTool(properties);
    }

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(prefix = "flink.http", name = "enabled", havingValue = "true")
    public FlinkCEPWebInitializer flinkCEPWebInitializer() {
        return new FlinkCEPWebInitializer();
    }
}