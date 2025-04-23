package org.start2do.bpm.dto.resp.flow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.camunda.bpm.engine.repository.ProcessDefinition;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowDefinitionDetailResp {

    private String id;
    private String name;
    private String key;
    private int version;
    private String deploymentId;
    private String resourceName;
    private String diagramResourceName;
    private boolean suspended;
    private String category;
    private String description;
    private String xml;

    public FlowDefinitionDetailResp(ProcessDefinition definition, String xmlContent) {
        this.id = definition.getId();
        this.name = definition.getName();
        this.key = definition.getKey();
        this.version = definition.getVersion();
        this.deploymentId = definition.getDeploymentId();
        this.resourceName = definition.getResourceName();
        this.diagramResourceName = definition.getDiagramResourceName();
        this.suspended = definition.isSuspended();
        this.category = definition.getCategory();
        this.description = definition.getDescription();
        // 设置XML内容
        this.xml = xmlContent;
    }
}
