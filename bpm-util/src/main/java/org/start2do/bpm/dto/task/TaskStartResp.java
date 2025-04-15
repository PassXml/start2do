package org.start2do.bpm.dto.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dromara.warm.flow.core.enums.FlowStatus;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskStartResp {

    private Long instanceId;
    private String businessId;
    private FlowStatus flowStatus;

    public String getFlowStatus() {
        return flowStatus.getKey();
    }

    public String getFlowStatusStr() {
        return flowStatus.getValue();
    }
}
