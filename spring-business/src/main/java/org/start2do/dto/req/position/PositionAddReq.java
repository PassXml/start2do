package org.start2do.dto.req.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

@Data
@Accessors(chain = true)
public class PositionAddReq {
    private String parentId;
    @NotBlank(message = "岗位名称不能为空")
    private String name;
    @NotBlank(message = "岗位编码不能为空")
    private String code;
    @NotNull(message = "状态不能为空")
    private EnableType status;
    private Integer sort;
    private String sourceType;
    private String sourceId;
}
