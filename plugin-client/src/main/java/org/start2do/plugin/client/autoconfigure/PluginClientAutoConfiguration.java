package org.start2do.plugin.client.autoconfigure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.start2do.plugin.client.config.PluginClientProperties;
import org.start2do.plugin.client.core.PluginNodeClient;
import org.start2do.plugin.service.PluginFileService;

/**
 * 插件客户端自动配置
 * <p>
 * 依赖 plugin-service 提供的本地插件文件与 PF4J 集成功能，
 * 通过 HTTP 与 plugin-server 进行交互。
 */
@Configuration
@EnableConfigurationProperties(PluginClientProperties.class)
@ComponentScan("org.start2do.plugin.client")
@ConditionalOnClass(RestTemplate.class)
@RequiredArgsConstructor
public class PluginClientAutoConfiguration {

    private final PluginClientProperties properties;

    /**
     * 默认 RestTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate pluginRestTemplate() {
        return new RestTemplate();
    }

    /**
     * 插件节点客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public PluginNodeClient pluginNodeClient(RestTemplate pluginRestTemplate,
        PluginFileService pluginFileService) {
        return new PluginNodeClient(properties, pluginRestTemplate, pluginFileService);
    }
}
