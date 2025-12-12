package org.start2do.plugin.server.web.dto;

import java.util.List;
import lombok.Data;

/**
 * 管理端按节点配置灰度时的请求体
 */
@Data
public class PluginGrayNodesRequest {

    /**
     * 灰度版本号（即 grayVersion）
     */
    private String version;

    /**
     * 需要下发灰度版本的节点 ID 列表
     */
    private List<String> nodeIds;

    /**
     * 操作人（可选）
     */
    private String operator;

    private String remark;
}

