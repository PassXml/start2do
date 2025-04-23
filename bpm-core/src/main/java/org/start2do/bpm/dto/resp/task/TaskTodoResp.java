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
public class TaskTodoResp {

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
     * 任务优先级
     */
    private int priority;
    /**
     * 任务创建时间
     */
    private Date createTime;
    /**
     * 任务到期时间
     */
    private Date dueDate;
    /**
     * 任务所属人
     */
    private String owner;
    /**
     * 任务办理人
     */
    private String assignee;
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    /**
     * 流程定义ID
     */
    private String processDefinitionId;
    /**
     * 任务定义Key
     */
    private String taskDefinitionKey;
    /**
     * 表单Key
     */
    private String formKey;
    /**
     * 任务状态
     */
    private String status;

    public TaskTodoResp(HistoricTaskInstance task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.createTime = task.getStartTime();
        this.dueDate = task.getDueDate();
        this.owner = task.getOwner();
        this.assignee = task.getAssignee();
        this.processInstanceId = task.getProcessInstanceId();
        this.processDefinitionId = task.getProcessDefinitionId();
        this.taskDefinitionKey = task.getTaskDefinitionKey();
    }
}
