package org.start2do.bpm.service;


import java.io.IOException;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.bpm.dto.req.flow.FlowDefinitionPageReq;
import org.start2do.dto.BusinessException;
import org.start2do.dto.Page;

@Slf4j
@Service
@RequiredArgsConstructor
public class BpmDefinitionsService {


    private final RepositoryService repositoryService;

    public Page<ProcessDefinition> page(@Valid FlowDefinitionPageReq req) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey()
            .asc().orderByProcessDefinitionVersion().desc().latestVersion();
        if (StringUtils.isNotEmpty(req.getName())) {
            query.processDefinitionNameLike(req.getName());
        }
        return new Page<>(query.count(), req.getSize(), req.getCurrent(),
            query.listPage(req.getOffset(), req.getSize()));
    }

    public Deployment deploy(@NotEmpty String processName, @NotEmpty String bpmnXml, MultipartFile file) {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(processName);
        if (StringUtils.isNotEmpty(bpmnXml)) {
            deploymentBuilder.addString(processName + ".bpmn", bpmnXml);
        }
        if (file != null) {
            try {
                deploymentBuilder.addInputStream(processName + ".bpmn", file.getInputStream());
            } catch (IOException e) {
                throw new BusinessException(e);
            }
        }
        return deploymentBuilder.deploy();
    }


    public void deleteDeployment(@NotEmpty String deploymentId, boolean cascade) {
        repositoryService.deleteDeployment(deploymentId, cascade);
    }

    public void deleteAllProcessDefinition(@NotEmpty String processDefinitionKey, boolean cascade) {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .list();

        for (ProcessDefinition definition : definitions) {
            repositoryService.deleteProcessDefinition(definition.getId(), cascade);
        }
    }

    public void deleteProcessDefinition(@NotEmpty String deploymentId, boolean cascade) {
        repositoryService.deleteProcessDefinition(deploymentId, cascade);
    }

    public void activateProcessDefinitionById(String id) {
        repositoryService.activateProcessDefinitionById(id, true, null);
    }

    public void suspendProcessDefinitionById(String id) {
        repositoryService.suspendProcessDefinitionById(id, true, null);
    }

}
