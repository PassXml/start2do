package org.start2do.bpm.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.start2do.bpm.dto.req.flow.FlowDefinitionAddReq;
import org.start2do.bpm.dto.req.flow.FlowDefinitionPageReq;
import org.start2do.bpm.dto.req.flow.FlowDefinitionUpdateReq;
import org.start2do.bpm.dto.resp.flow.FlowDefinitionPageResp;
import org.start2do.bpm.service.BpmDefinitionsService;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.util.ValidateException;

/**
 * 流程定义
 */
@Slf4j
@Controller
@RequestMapping("/flow/definitions")
@RequiredArgsConstructor
public class DefinitionController {

    private final BpmDefinitionsService bpmService;

    /**
     * 分页
     */
    @GetMapping("page")
    @ResponseBody
    public R<Page<FlowDefinitionPageResp>> page(@Valid FlowDefinitionPageReq req) {
        return R.ok(bpmService.page(req).map(FlowDefinitionPageResp::new));
    }

    /**
     * 部署
     */
    @PostMapping("add")
    @ResponseBody
    public R<String> deploy(@Valid FlowDefinitionAddReq req) {
        if (StringUtils.isEmpty(req.getXml()) && req.getFile() == null) {
            throw new ValidateException("xml或者文件不能为空");
        }
        Deployment deploy = bpmService.deploy(req.getName(), req.getXml(), req.getFile());
        return R.ok(deploy.getId());
    }

    /**
     * 更新
     */
    @PostMapping("update")
    @ResponseBody
    public R<String> update(@Valid FlowDefinitionUpdateReq req) {
        if (StringUtils.isEmpty(req.getXml()) && req.getFile() == null) {
            throw new ValidateException("xml或者文件不能为空");
        }
        return R.ok(bpmService.deploy(req.getName(), req.getXml(), req.getFile()).getId());
    }

    /**
     * 删除
     */
    @ResponseBody
    @GetMapping("deleteByProcessDefinitionKey")
    public R<Void> deleteByProcessDefinitionKey(@RequestParam("processDefinitionKey") String processDefinitionKey) {
        bpmService.deleteAllProcessDefinition(processDefinitionKey, true);
        return R.ok();
    }

    /**
     * 激活
     */
    @ResponseBody
    @GetMapping("activate")
    public R<Void> activate(@RequestParam("id") String id) {
        bpmService.activateProcessDefinitionById(id);
        return R.ok();
    }

    /**
     * 挂起
     */
    @ResponseBody
    @GetMapping("suspend")
    public R<Void> suspend(@RequestParam("id") String id) {
        bpmService.suspendProcessDefinitionById(id);
        return R.ok();
    }
}
