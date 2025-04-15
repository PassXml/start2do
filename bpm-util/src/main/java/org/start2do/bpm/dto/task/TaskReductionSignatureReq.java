package org.start2do.bpm.dto.task;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class TaskReductionSignatureReq {

    /**
     * 流程实例id
     */
    @NotNull
    private Long instanceId;
    /**
     * 转办原因
     */
    private String message;
    /**
     * 转办人
     */
    @NotEmpty
    private List<String> reductionSignature;
    /**
     * 权限标识
     */
    @NotEmpty
    private String permissionFlag;
}
