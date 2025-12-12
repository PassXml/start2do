package com.start2do.plugin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.start2do.plugin.config.PluginSystemProperties;

/**
 * 插件管理服务启动类（测试用）
 */
@SpringBootApplication
@EnableConfigurationProperties(PluginSystemProperties.class)
public class PluginServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PluginServiceApplication.class, args);
    }
}

