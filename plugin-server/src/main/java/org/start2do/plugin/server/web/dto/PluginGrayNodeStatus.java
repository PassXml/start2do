package org.start2do.plugin.server.web.dto;

import lombok.Data;

/**
 * 管理端查看 / 编辑某插件灰度节点时使用的节点视图
 */
@Data
public class PluginGrayNodeStatus {

    /**
     * 节点唯一标识
     */
    private String nodeId;

    /**
     * 应用名
     */
    private String appName;

    private String ip;
    private Integer port;

    /**
     * 节点上当前运行的该插件版本（可能为空）
     */
    private String currentVersion;

    /**
     * 当前发布配置中的稳定版本
     */
    private String stableVersion;

    /**
     * 当前发布配置中的灰度版本
     */
    private String grayVersion;

    /**
     * 是否在灰度节点列表中（即 server 指定该节点下发灰度版本）
     */
    private boolean inGray;

    /**
     * 该节点在同步此插件版本时最近一次错误信息（如路由冲突等），可为空
     */
    private String lastError;

    /**
     * 最近心跳时间
     * <p>
     * 格式：yyyy-MM-dd HH:mm:ss，时区采用系统默认时区。
     */
    private String lastHeartbeatTime;
}
