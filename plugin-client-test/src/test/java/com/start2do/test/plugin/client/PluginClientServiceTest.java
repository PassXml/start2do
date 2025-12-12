package com.start2do.test.plugin.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.start2do.plugin.client.config.PluginClientProperties;
import org.start2do.plugin.client.core.PluginNodeClient;

/**
 * plugin-client 测试服务用例：
 * <p>
 * 1. 启动一个最小化的 Spring Boot 上下文；
 * 2. 验证 PluginNodeClient / PluginClientProperties 是否正常装配；
 * 3. 通过 HTTP 访问测试接口，检查配置是否正确返回。
 */
@SpringBootTest(
    classes = PluginClientTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class PluginClientServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PluginNodeClient pluginNodeClient;

    @Autowired
    private PluginClientProperties properties;

    @Test
    void contextLoadsAndBeansCreated() {
        assertNotNull(pluginNodeClient, "PluginNodeClient Bean 不应为 null");
        assertNotNull(properties, "PluginClientProperties Bean 不应为 null");
        assertEquals("http://localhost:8082", properties.getServerBaseUrl());
        assertEquals("plugin-test-client", properties.getAppName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void httpEndpointShouldExposeConfig() {
        String url = "http://localhost:" + port + "/test/plugin-client/config";
        Map<String, Object> body = restTemplate.getForObject(url, Map.class);
        assertNotNull(body, "返回结果不应为 null");
        assertEquals("http://localhost:8082", body.get("serverBaseUrl"));
        assertEquals("plugin-test-client", body.get("appName"));
        assertEquals("plugin-test-node-1", body.get("nodeId"));
    }
}
