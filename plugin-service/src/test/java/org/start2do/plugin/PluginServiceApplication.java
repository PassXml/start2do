package org.start2do.plugin;

import org.start2do.plugin.config.PluginStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 插件管理服务启动类
 */
@SpringBootApplication
@EnableConfigurationProperties(PluginStorageProperties.class)
public class PluginServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PluginServiceApplication.class, args);
    }
}

