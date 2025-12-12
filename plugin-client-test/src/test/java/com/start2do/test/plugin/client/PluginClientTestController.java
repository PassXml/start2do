package com.start2do.test.plugin.client;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.plugin.client.config.PluginClientProperties;

/**
 * 简单的 plugin-client 测试服务接口
 * <p>
 * 主要用于验证配置是否正确绑定，以及提供一个可手动访问的示例 HTTP 接口。
 */
@RestController
@RequestMapping("/test/plugin-client")
@RequiredArgsConstructor
public class PluginClientTestController {

    private final PluginClientProperties properties;

    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> map = new HashMap<>();
        map.put("serverBaseUrl", properties.getServerBaseUrl());
        map.put("appName", properties.getAppName());
        map.put("nodeId", properties.getNodeId());
        map.put("heartbeatIntervalSeconds", properties.getHeartbeatIntervalSeconds());
        return map;
    }
}

