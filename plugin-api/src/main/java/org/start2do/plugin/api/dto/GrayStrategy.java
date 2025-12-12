package org.start2do.plugin.api.dto;

import java.util.List;
import lombok.Data;

/**
 * 灰度发布策略定义
 */
@Data
public class GrayStrategy {

    /**
     * 灰度策略类型：
     * NODE：由服务端显式指定 nodeId 列表，只有这些节点下发灰度版本
     * （如需扩展其它类型，可在此基础上增加）
     */
    private String type;

    /**
     * 通用数值参数（保留扩展用，目前不强制使用）
     */
    private Integer value;

    /**
     * 需要下发灰度版本的节点 ID 列表
     * <p>
     * 当 type = NODE 时生效，其它类型可忽略。
     */
    private List<String> nodeIds;
}
