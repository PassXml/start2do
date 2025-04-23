package org.start2do.bpm.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.UserTaskImpl;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.springframework.stereotype.Service;
import org.start2do.bpm.interfaces.ITokenUtil;
import org.start2do.dto.Page;

@Slf4j
@Service
@RequiredArgsConstructor
public class BpmTaskService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final ITokenUtil tokenUtil;

    public Page<HistoricTaskInstance> getTodoTasks(Page page, String username) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
            .taskHadCandidateUser(username).or()
            .taskAssignee(username)
            .unfinished().orderByHistoricActivityInstanceStartTime().desc();
        return new Page<>(query.count(), page.getSize(), page.getCurrent(),
            query.listPage(page.getOffset(), page.getSize()));
    }

    public Page<HistoricTaskInstance> getDoneTasks(Page page, String username) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
            .taskHadCandidateUser(username).or()
            .taskAssignee(username)
            .finished().orderByHistoricActivityInstanceStartTime().desc();
        return new Page<>(query.count(), page.getSize(), page.getCurrent(),
            query.listPage(page.getOffset(), page.getSize()));
    }

    public String startProcess(@NotEmpty String processDefinitionKey, String businessKey,
        Map<String, Object> variables) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey,
            variables);
        return processInstance.getProcessInstanceId();
    }

    public void completeTask(String taskId, Map<String, Object> variables,Map<String, Object> localVariables) {
        taskService.setVariablesLocal(taskId, localVariables);
        if (variables != null) {
            taskService.complete(taskId, variables);
        }else{
            taskService.complete(taskId);
        }
    }

    /**
     * 获取任务审批历史
     *
     * @param processInstanceId 任务ID
     * @return 历史任务实例列表
     */
    public List<HistoricTaskInstance> getTaskHistory(String processInstanceId) {
        return historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
            .orderByHistoricActivityInstanceStartTime().desc().list();
    }

    /**
     * 转办
     */
    public void transferTask(String taskId, String assignee) {
        taskService.setAssignee(taskId, assignee);
    }

    /**
     * 驳回任务
     *
     * @param taskId           当前任务ID
     * @param targetActivityId 目标活动节点ID
     */
    public void rejectTask(@NotEmpty String taskId, @NotEmpty String targetActivityId, String username) {
        // 获取当前任务
        org.camunda.bpm.engine.task.Task task = taskService.createTaskQuery().taskId(taskId).taskAssignee(
            username
        ).singleResult();
        // 创建驳回流程
        runtimeService.createProcessInstanceModification(task.getProcessInstanceId())
            .cancelAllForActivity(task.getTaskDefinitionKey()).startBeforeActivity(targetActivityId).execute();
    }

    /**
     * 查询可驳回节点列表
     *
     * @param taskId 当前任务ID
     * @return 可驳回节点ID列表
     */
    public List<String> getRejectableNodes(@NotEmpty String taskId, String username) {
        org.camunda.bpm.engine.task.Task task = taskService.createTaskQuery().taskId(taskId).taskAssignee(
            username
        ).or().taskCandidateUser(username).singleResult();
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(task.getProcessDefinitionId());
        UserTaskImpl currentElement = modelInstance.getModelElementById(task.getTaskDefinitionKey());
        // 使用集合存储可驳回的节点ID，避免重复
        Set<String> rejectableNodeIds = new HashSet<>();
        // 使用集合存储已访问的节点ID，避免循环引用
        Set<String> visitedNodeIds = new HashSet<>();
        // 从当前节点开始，向前遍历所有节点
        findPrecedingUserTasks(modelInstance, currentElement, rejectableNodeIds, visitedNodeIds);
        // 移除当前节点（如果存在）
        rejectableNodeIds.remove(currentElement.getId());
        return new ArrayList<>(rejectableNodeIds);
    }

    /**
     * 递归查找前置用户任务节点
     *
     * @param modelInstance     BPMN模型
     * @param element           当前元素
     * @param rejectableNodeIds 结果集，存储可驳回的节点ID
     * @param visitedNodeIds    已访问节点集合，避免循环引用
     */
    private void findPrecedingUserTasks(BpmnModelInstance modelInstance, FlowNode element,
        Set<String> rejectableNodeIds, Set<String> visitedNodeIds) {
        // 标记当前节点为已访问
        visitedNodeIds.add(element.getId());
        // 获取所有传入连线
        Collection<SequenceFlow> incoming = element.getIncoming();
        for (SequenceFlow flow : incoming) {
            FlowNode sourceNode = flow.getSource();
            // 如果是用户任务节点，则加入可驳回列表
            if (sourceNode instanceof UserTask) {
                rejectableNodeIds.add(sourceNode.getId());
            }
            // 避免重复访问同一节点，防止无限循环
            if (!visitedNodeIds.contains(sourceNode.getId())) {
                // 递归查找前置节点
                findPrecedingUserTasks(modelInstance, sourceNode, rejectableNodeIds, visitedNodeIds);
            }
        }
    }
}
