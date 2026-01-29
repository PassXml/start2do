package com.start2do;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.start2do.plugin.config.PluginSystemProperties;

/**
 * 插件管理服务启动类
 * <p>
 * 负责统一管理插件版本、灰度发布与节点注册信息。
 */
@SpringBootApplication
@EnableConfigurationProperties(PluginSystemProperties.class)
public class PluginServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PluginServerApplication.class, args);
    }
}

