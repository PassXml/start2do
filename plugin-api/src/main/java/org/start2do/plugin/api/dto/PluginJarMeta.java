package org.start2do.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 插件 Jar 元数据信息（pluginId + version）。
 * <p>
 * 该对象只关心与 PF4J 对齐的插件标识与版本号，
 * 具体从何处解析（plugin.properties 或 Manifest）由工具类处理。
 */
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class PluginJarMeta {

    /**
     * 插件唯一标识，对齐 PF4J 的 plugin.id。
     */
    private String pluginId;

    /**
     * 插件版本号，对齐 PF4J 的 plugin.version。
     */
    private String version;

    /**
     * 按约定生成标准文件名：pluginId-version.jar
     */
    public String getFileName() {
        return pluginId.trim() + "-" + version.trim() + ".jar";
    }
}

