package org.start2do.plugin.server.web.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 节点注册 / 心跳上报请求
 */
@Data
public class NodeRegisterRequest {

    private String nodeId;
    private String appName;
    private String ip;
    private Integer port;

    /**
     * 节点标签（分组、环境等）
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * 节点当前插件版本
     * key: pluginId, value: version
     */
    private Map<String, String> plugins = new HashMap<>();

    /**
     * 节点上各插件最近一次同步错误信息（可选）
     * key: pluginId, value: error message
     */
    private Map<String, String> pluginErrors = new HashMap<>();
}
