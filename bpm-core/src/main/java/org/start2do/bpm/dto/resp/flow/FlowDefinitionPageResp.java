package org.start2do.bpm.dto.resp.flow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.start2do.constant.Constant;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowDefinitionPageResp {

    private String id;
    private String deploymentId;
    private String name;
    private Integer version;
    private String processDefinitionKey;
    private String category;
    private String status;
    private String description;

    public FlowDefinitionPageResp(ProcessDefinition definition) {
        this.id = definition.getId();
        this.deploymentId = definition.getDeploymentId();
        this.name = definition.getName();
        this.version = definition.getVersion();
        this.processDefinitionKey = definition.getKey();
        this.description = definition.getDescription();
        this.category = definition.getCategory();
        this.status = definition.isSuspended() ? Constant.DISABLE : Constant.ENABLE;
    }
}
