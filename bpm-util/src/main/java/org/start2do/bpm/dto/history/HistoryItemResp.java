package org.start2do.bpm.dto.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dromara.warm.flow.core.entity.HisTask;
import org.dromara.warm.flow.core.enums.FlowStatus;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class HistoryItemResp {

    private Long taskId;
    private Long instanceId;
    private String message;
    private FlowStatus flowStatus;

    private String nodeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String approver;
    private String ext;
    private String formPath;

    public HistoryItemResp(HisTask hisTask) {
        this.taskId = hisTask.getTaskId();
        this.instanceId = hisTask.getInstanceId();
        this.message = hisTask.getMessage();
        this.flowStatus = FlowStatus.getByKey(hisTask.getFlowStatus());
        this.nodeName = hisTask.getNodeName();
        this.createTime = hisTask.getCreateTime();
        this.updateTime = hisTask.getUpdateTime();
        this.approver = hisTask.getApprover();
        this.ext = hisTask.getExt();
        this.formPath = hisTask.getFormPath();
    }

    public String getFlowStatus() {
        return flowStatus.getKey();
    }

    public String getFlowStatusStr() {
        return flowStatus.getValue();
    }

}
