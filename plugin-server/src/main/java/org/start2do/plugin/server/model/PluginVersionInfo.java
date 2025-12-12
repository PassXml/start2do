package org.start2do.plugin.server.model;

import lombok.Data;

/**
 * 单个插件版本的元数据信息
 */
@Data
public class PluginVersionInfo {

    /**
     * 插件唯一标识（通常与 PF4J 中的 Plugin-Id 对应）
     */
    private String pluginId;

    /**
     * 版本号，例如 1.0.0
     */
    private String version;

    /**
     * 版本当前状态：DRAFT / GRAY / STABLE / ROLLED_BACK
     */
    private String status;

    /**
     * 插件 JAR 文件相对路径（相对于存储根目录）
     */
    private String jarPath;

    /**
     * 插件 JAR 文件的校验和（可选，用于完整性校验）
     */
    private String checksum;

    /**
     * 版本说明或变更记录
     */
    private String changelog;
}

