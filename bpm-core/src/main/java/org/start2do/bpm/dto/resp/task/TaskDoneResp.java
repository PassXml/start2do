package org.start2do.bpm.dto.resp.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.camunda.bpm.engine.history.HistoricTaskInstance;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskDoneResp {

    /**
     * 任务ID
     */
    private String id;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 任务描述
     */
    private String description;
    /**
     * 任务定义Key
     */
    private String taskDefinitionKey;
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 持续时间(毫秒)
     */
    private Long durationInMillis;
    /**
     * 任务处理人
     */
    private String assignee;
    /**
     * 状态
     */
    private String status;

    public TaskDoneResp(HistoricTaskInstance instance) {
        this.id = instance.getId();
        this.name = instance.getName();
        this.description = instance.getDescription();
        this.taskDefinitionKey = instance.getTaskDefinitionKey();
        this.processInstanceId = instance.getProcessInstanceId();
        this.startTime = instance.getStartTime();
        this.endTime = instance.getEndTime();
        this.durationInMillis = instance.getDurationInMillis();
        this.assignee = instance.getAssignee();
        this.status = instance.getDeleteReason();
    }

}
