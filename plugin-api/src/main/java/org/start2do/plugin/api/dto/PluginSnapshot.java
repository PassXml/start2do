package org.start2do.plugin.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 管理端返回给节点的插件发布快照
 */
@Data
public class PluginSnapshot {

    /**
     * 当前所有插件的发布配置
     */
    private List<PluginReleaseConfig> releases = new ArrayList<PluginReleaseConfig>();
}

