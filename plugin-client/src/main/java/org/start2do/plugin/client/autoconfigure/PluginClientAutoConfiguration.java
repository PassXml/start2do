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
import org.start2do.plugin.client.http.AuthTokenInterceptor;
import org.start2do.plugin.service.PluginFileService;

import java.util.Collections;

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
     * 认证令牌拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthTokenInterceptor authTokenInterceptor() {
        return new AuthTokenInterceptor(properties);
    }

    /**
     * 默认 RestTemplate，配置认证拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate pluginRestTemplate(AuthTokenInterceptor authTokenInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(authTokenInterceptor));
        return restTemplate;
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
