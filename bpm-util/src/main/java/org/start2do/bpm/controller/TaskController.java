package org.start2do.bpm.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.enums.FlowStatus;
import org.dromara.warm.flow.core.enums.SkipType;
import org.dromara.warm.flow.core.service.InsService;
import org.dromara.warm.flow.core.service.NodeService;
import org.dromara.warm.flow.core.service.TaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.bpm.dto.task.TaskPassReq;
import org.start2do.bpm.dto.task.TaskReductionSignatureReq;
import org.start2do.bpm.dto.task.TaskStartReq;
import org.start2do.bpm.dto.task.TaskStartResp;
import org.start2do.bpm.dto.task.TaskTransFefReq;
import org.start2do.bpm.service.IUserHandle;
import org.start2do.dto.R;

/**
 * 代办任务
 */
@RestController
@RequestMapping("task")
@RequiredArgsConstructor
public class TaskController {

    private final InsService insService;
    private final TaskService taskService;
    private final IUserHandle iUserHandle;
    private final NodeService nodeService;

    /**
     * 开始流程
     */
    @PostMapping("start")
    public R<TaskStartResp> start(@Valid @RequestBody TaskStartReq req) {
        return start(req, false);
    }

    /**
     * 开始流程
     */
    @PostMapping("startAndSubmit")
    public R<TaskStartResp> startAndSubmit(@Valid @RequestBody TaskStartReq req) {
        return start(req, true);
    }

    private R<TaskStartResp> start(TaskStartReq req, Boolean autoSubmit) {
        Map<String, Object> variable = req.getVariable();
        if (variable == null) {
            variable = new HashMap<>();
        }
        if (req.getBusinessId() == null || req.getBusinessId().isEmpty()) {
            req.setBusinessId(UUID.randomUUID().toString().replace("-", ""));
        }
        variable.put("starter", iUserHandle.getCurrentUsername());
        FlowParams params = new FlowParams().flowCode(req.getFlowCode()).handler(iUserHandle.getCurrentUsername())
            .variable(variable);
        if (autoSubmit) {
            params.skipType(FlowStatus.APPROVAL.getKey());
        }
        Instance instance = insService.start(req.getBusinessId(), params);
        return R.ok(new TaskStartResp(instance.getId(), instance.getBusinessId()));
    }

    /**
     * 终止流程
     */
    @PostMapping("stop")
    public R start(Long instanceId) {
        Instance instance = insService.termination(instanceId, null);
        return R.ok(new TaskStartResp(instance.getId(), instance.getBusinessId()));
    }

    /**
     * 通过
     */
    @PostMapping("pass")
    public R pass(@RequestBody @Valid TaskPassReq req) {
        Instance instance = insService.skipByInsId(req.getInstanceId(),
            new FlowParams().skipType(SkipType.PASS.getKey()).message(req.getMessage()).variable(req.getVariable())
                .handler(req.getHandler()));
        return R.ok(new TaskStartResp(instance.getId(), instance.getBusinessId()));
    }

    /**
     * 拒绝
     */
    @PostMapping("reject")
    public R reject(@RequestBody @Valid TaskPassReq req) {
        Instance instance = insService.skipByInsId(req.getInstanceId(),
            new FlowParams().skipType(SkipType.REJECT.getKey()).message(req.getMessage()).variable(req.getVariable())
                .handler(req.getHandler()));
        return R.ok(new TaskStartResp(instance.getId(), instance.getBusinessId()));
    }

    /**
     * 转办
     */
    @PostMapping("transfer")
    public R<Boolean> transfer(@RequestBody @Valid TaskTransFefReq req) {
        return R.ok(taskService.transfer(req.getInstanceId(),
            new FlowParams().handler(iUserHandle.getCurrentUsername()).message(req.getMessage())
                .addHandlers(req.getAddHandler()).permissionFlag(Arrays.asList(req.getPermissionFlag().split("\\|")))));
    }

    /**
     * 委托
     */
    @PostMapping("depute")
    public R<Boolean> depute(@RequestBody @Valid TaskTransFefReq req) {
        return R.ok(taskService.depute(req.getInstanceId(),
            new FlowParams().handler(iUserHandle.getCurrentUsername()).message(req.getMessage())
                .addHandlers(req.getAddHandler()).permissionFlag(Arrays.asList(req.getPermissionFlag().split("\\|")))));
    }

    /**
     * 加签
     */
    @PostMapping("addSign")
    public R<Boolean> addSign(@RequestBody @Valid TaskTransFefReq req) {
        return R.ok(taskService.addSignature(req.getInstanceId(),
            new FlowParams().handler(iUserHandle.getCurrentUsername()).message(req.getMessage())
                .addHandlers(req.getAddHandler())));
    }

    /**
     * 减签
     */
    @PostMapping("reductionSignature")
    public R<Boolean> reductionSignature(@RequestBody @Valid TaskReductionSignatureReq req) {
        return R.ok(taskService.reductionSignature(req.getInstanceId(),
            new FlowParams().handler(iUserHandle.getCurrentUsername()).message(req.getMessage())
                .reductionHandlers(req.getReductionSignature())
                .permissionFlag(Arrays.asList(req.getPermissionFlag().split("\\|")))));
    }
//    @PostMapping("updateHandler")
//    public R<Boolean> updateHandler(@RequestBody @Valid TaskTransFefReq req) {
//        return R.ok(taskService.updateHandler(
//            req.getInstanceId(),
//            new FlowParams().handler(iUserHandle.getCurrentUsername()).message(req.getMessage()).addHandlers(
//                req.getAddHandler()
//            ).permissionFlag(Arrays.asList(req.getPermissionFlag().split("\\|")))));
//    }

}
