package org.start2do.bpm.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.bpm.dto.Constant;
import org.start2do.bpm.dto.req.task.TaskAgreeReq;
import org.start2do.bpm.dto.req.task.TaskStartReq;
import org.start2do.bpm.dto.resp.task.TaskDoneResp;
import org.start2do.bpm.dto.resp.task.TaskTodoResp;
import org.start2do.bpm.interfaces.ITokenUtil;
import org.start2do.bpm.service.BpmTaskService;
import org.start2do.dto.Page;
import org.start2do.dto.R;

/**
 * 流程发起
 */
@Slf4j
@RestController
@RequestMapping("task")
@RequiredArgsConstructor
public class TaskController {

    private final ITokenUtil tokenUtil;
    private final BpmTaskService bpmTaskService;

    /**
     * 流程发起
     */
    @PostMapping("start")
    public R<String> startProcess(@RequestBody @Valid TaskStartReq req) {
        if (StringUtils.isEmpty(req.getBusinessKey())) {
            req.setBusinessKey(UUID.randomUUID().toString().replace("-", ""));
        }
        Map<String, Object> variables = req.getVariables();
        if (variables == null) {
            variables = new HashMap<>(10);
        }
        variables.put(Constant.STARTER, tokenUtil.getCurUserName());
        return R.ok(bpmTaskService.startProcess(req.getProcessDefinitionKey(), req.getBusinessKey(), variables));
    }

    /**
     * 任务审批
     */
    @PostMapping("complete")
    public R completeTask(@Valid TaskAgreeReq req) {
        Map<String, Object> localMap = req.getVariables();
        localMap.put(Constant.AGREE, req.isAgree());
        localMap.put(Constant.MSG, req.isAgree());
        if (StringUtils.isNotEmpty(req.getAssignee())) {
            localMap.put(Constant.ASSIGNEE, req.getAssignee());
        }
        bpmTaskService.completeTask(req.getTaskId(), null, localMap);
        return R.ok();
    }

    /**
     * 查询待办
     */
    @GetMapping("page")
    public R<Page<TaskTodoResp>> page(Page page) {
        return R.ok(bpmTaskService.getTodoTasks(page, tokenUtil.getCurUserName()).map(TaskTodoResp::new));
    }

    /**
     * 查询已办
     */
    @GetMapping("done/page")
    public R<Page<TaskDoneResp>> donePage(Page page) {
        return R.ok(bpmTaskService.getDoneTasks(page, tokenUtil.getCurUserName()).map(TaskDoneResp::new));
    }

    /**
     * 审批历史
     */
    @GetMapping("detail")
    public R detail(String id) {
        return R.ok(bpmTaskService.getTaskHistory(id));
    }

    /**
     * 转办
     */
    @GetMapping("transferTask")
    public R transferTask(@RequestParam("taskId") String taskId, @RequestParam("assignee") String assignee) {
        bpmTaskService.transferTask(taskId, assignee);
        return R.ok();
    }

    @GetMapping("rejectTask")
    public R rejectTask(@RequestParam("taskId") String taskId,
        @RequestParam("targetActivityId") String targetActivityId) {
        bpmTaskService.rejectTask(taskId, targetActivityId, tokenUtil.getCurUserName());
        return R.ok();
    }

    @GetMapping("getRejectableNodes")
    public R getRejectableNodes(@RequestParam("taskId") String taskId) {
        return R.ok(bpmTaskService.getRejectableNodes(taskId, tokenUtil.getCurUserName()));
    }

}
