package org.start2do.bpm.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessInstanceViewerResp {

    private String bpmnXml; // 流程定义的XML内容
    // 已完成活动节点的ID列表
    private List<String> completedActivityIds;
    // 当前活动节点的ID列表
    private List<String> currentActivityIds;
    // 活动历史详情 (Key: activityId)
    private Map<String, ActivityHistoryInfoDTO> activityHistoryDetails;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivityHistoryInfoDTO {

        private String activityId;
        private String activityName;
        private String activityType;
        private String assignee;
        private Date startTime;
        private Date endTime;
        private Long durationInMillis;
        private String taskId; // 关联的任务ID (如果有)
        private String executionId;

        public ActivityHistoryInfoDTO(String activityId, String activityName, String activityType, String assignee,
            Date startTime, Date endTime, Long durationInMillis, String taskId, String executionId) {
            this.activityId = activityId;
            this.activityName = activityName;
            this.activityType = activityType;
            this.assignee = assignee;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationInMillis = durationInMillis;
            this.taskId = taskId;
            this.executionId = executionId;
        }
    }
}
