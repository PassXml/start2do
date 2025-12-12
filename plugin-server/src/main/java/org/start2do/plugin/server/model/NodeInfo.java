package org.start2do.plugin.server.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 业务节点注册信息
 */
@Data
public class NodeInfo {

    /**
     * 节点唯一标识（由客户端生成并上报）
     */
    private String nodeId;

    /**
     * 节点所在应用名称
     */
    private String appName;

    private String ip;
    private Integer port;

    /**
     * 节点标签（可用于分组灰度）
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * 节点当前已加载的插件版本
     * key: pluginId, value: version
     */
    private Map<String, String> plugins = new HashMap<>();

    /**
     * 节点上各插件最近一次同步错误信息
     * key: pluginId, value: error message
     */
    private Map<String, String> pluginErrors = new HashMap<>();

    /**
     * 上次心跳时间
     * <p>
     * 格式：yyyy-MM-dd HH:mm:ss，时区采用系统默认时区。
     */
    private String lastHeartbeatTime;
}
