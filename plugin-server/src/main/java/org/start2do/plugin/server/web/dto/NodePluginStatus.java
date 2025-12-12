package org.start2do.plugin.server.web.dto;

import lombok.Data;

/**
 * 节点上某个插件的状态视图
 */
@Data
public class NodePluginStatus {

    /**
     * 节点唯一标识
     */
    private String nodeId;

    private String appName;
    private String ip;
    private Integer port;

    /**
     * 节点当前运行的插件版本（可能为空）
     */
    private String currentVersion;

    /**
     * 根据发布配置计算出的目标版本（stable 或 gray）
     */
    private String targetVersion;

    /**
     * 目标版本类型：STABLE / GRAY / NONE
     */
    private String targetType;

    /**
     * 同步状态：
     * SYNCED        - currentVersion 与 targetVersion 一致；
     * OUT_OF_SYNC   - 有目标版本但本地版本不一致；
     * NOT_INSTALLED - 有目标版本但本地未安装该插件；
     * NO_TARGET     - 当前没有为该插件配置目标版本。
     */
    private String syncStatus;

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
