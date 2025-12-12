package com.start2do.test.plugin.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.start2do.plugin.config.PluginSystemProperties;

/**
 * plugin-server 测试用 Spring Boot 启动类
 * <p>
 * 只在测试范围内启动管理端的 Web 接口，验证基础接口是否可用。
 * <p>
 * 注意：这里显式排除了 plugin-client 的自动配置，避免在管理端进程中
 * 自动创建 PluginNodeClient 并定时向（可能并不存在的）8082 管理端心跳。
 */
@SpringBootApplication
@EnableConfigurationProperties(PluginSystemProperties.class)
public class PluginServerTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PluginServerTestApplication.class, args);
    }
}
