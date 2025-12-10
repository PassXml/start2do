package org.start2do.plugin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 插件文件信息，用于插件管理接口的返回
 */
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class PluginInfo {

    private String pluginId;
    /**
     * 文件名，例如 xxx.jar / xxx.jar.disable
     */
    private String fileName;

    /**
     * 是否启用（true: xxx.jar，false: xxx.jar.disable）
     */
    private boolean enabled;

    /**
     * 文件绝对路径
     */
    private String fullPath;

    public PluginInfo(String pluginId, String fileName, boolean enabled, String fullPath) {
        this.pluginId = pluginId;
        this.fileName = fileName;
        this.enabled = enabled;
        this.fullPath = fullPath;
    }
}

