package org.start2do.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class PluginJarMetaDto {

    private String pluginId;
    private String pluginVersion;

    public String getFileName() {
        return pluginId.trim() + "-" + pluginVersion.trim() + ".jar";
    }
}
