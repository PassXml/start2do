package org.start2do.plugin.server.web.dto;

import java.util.List;
import lombok.Data;

/**
 * 插件发布 / 灰度 / 回滚 请求
 */
@Data
public class PluginDeployRequest {

    /**
     * 目标插件版本
     */
    private String version;

    /**
     * 操作模式：
     * FULL：全量发布为稳定版本
     * GRAY：灰度发布
     * ROLLBACK：回滚到指定版本
     */
    private String mode;

    /**
     * 灰度策略 JSON 字段
     */
    private String grayType;
    private Integer grayValue;

    /**
     * 灰度节点列表（由服务端显式指定哪些 nodeId 下发灰度版本）
     */
    private List<String> grayNodeIds;

    /**
     * 操作人（可选）
     */
    private String operator;

    private String remark;
}
