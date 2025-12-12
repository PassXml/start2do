package com.start2do.test.plugin.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * plugin-client 测试用 Spring Boot 启动类
 * <p>
 * 只在测试范围内使用，用于验证配置绑定与 Bean 装配，不参与实际插件打包。
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PluginClientTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PluginClientTestApplication.class, args);
    }
}
