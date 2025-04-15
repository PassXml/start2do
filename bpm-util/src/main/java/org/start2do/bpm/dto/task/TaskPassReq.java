package org.start2do.bpm.dto.task;

import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class TaskPassReq {

    /**
      *  流程实例id
     */
    @NotNull
    private Long instanceId;
    /**
      *  审批意见
     */
    private String message;
    /**
      * 流程变量
     */
    private Map<String, Object> variable;
    /**
      *  处理人
     */
    private String handler;
}
