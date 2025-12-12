package com.start2do.test.plugin.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.start2do.plugin.api.dto.ApiResponse;
import org.start2do.plugin.server.web.dto.NodeRegisterRequest;

/**
 * plugin-server 测试服务用例：
 * <p>
 * 1. 启动管理端 Web 接口；
 * 2. 通过 /api/plugins/snapshot 验证发布快照接口；
 * 3. 通过 /api/nodes/register + /api/nodes 验证节点注册与查询。
 */
@SpringBootTest(
    classes = PluginServerTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class PluginServerServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @SuppressWarnings("unchecked")
    void snapshotEndpointShouldReturnSuccess() {
        String url = "http://localhost:" + port + "/api/plugins/snapshot";
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity(url, ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
        ApiResponse<?> body = resp.getBody();
        assertNotNull(body, "响应体不应为 null");
        assertEquals(0, body.getCode(), "业务返回码应为 0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void nodeRegisterAndListShouldWork() {
        // 1. 注册一个测试节点
        NodeRegisterRequest request = new NodeRegisterRequest();
        request.setNodeId("test-node-from-plugin-test");
        request.setAppName("plugin-test-client");
        request.setIp("127.0.0.1");
        request.setPort(0);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        request.setTags(tags);

        String registerUrl = "http://localhost:" + port + "/api/nodes/register";
        ApiResponse<?> registerResp =
            restTemplate.postForObject(registerUrl, request, ApiResponse.class);
        assertNotNull(registerResp, "注册接口返回不应为 null");
        assertEquals(0, registerResp.getCode(), "节点注册接口业务返回码应为 0");

        // 2. 查询节点列表，应至少包含刚才注册的节点
        String listUrl = "http://localhost:" + port + "/api/nodes";
        ApiResponse<?> listResp = restTemplate.getForObject(listUrl, ApiResponse.class);
        assertNotNull(listResp, "节点列表接口返回不应为 null");
        assertEquals(0, listResp.getCode(), "节点列表接口业务返回码应为 0");
    }
}
