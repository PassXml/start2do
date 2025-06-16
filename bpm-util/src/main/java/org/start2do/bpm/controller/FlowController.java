package org.start2do.bpm.controller;

import java.io.IOException;
import java.util.Collections;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.entity.Definition;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.orm.entity.FlowDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.start2do.bpm.dto.flow.FlowAddReq;
import org.start2do.bpm.dto.flow.FlowDeployReq;
import org.start2do.bpm.dto.flow.FlowDeployReq.Type;
import org.start2do.dto.Page;
import org.start2do.dto.R;

/**
 * 流程定义
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("flow")
public class FlowController {

    private final DefService defService;

    /**
     * 分页
     */
    @ResponseBody
    @GetMapping("page")
    public R<Page<Definition>> page(Page page) {
        org.dromara.warm.flow.core.utils.page.Page<Definition> result = defService.page(new FlowDefinition(),
            new org.dromara.warm.flow.core.utils.page.Page<>());
        return R.ok(new Page<>(result.getTotal(), result.getPageSize(), page.getCurrent(), result.getList()));
    }

    /**
     * 保存
     */
    @ResponseBody
    @PostMapping("add")
    public R<Boolean> add(@Valid @RequestBody FlowAddReq req) {
        boolean save = defService.save(new FlowDefinition().setFlowCode(req.getFlowCode())
            .setFlowName(req.getFlowName()).setCategory(req.getCategory()).setFormCustom(req.getFormCustom())
            .setFormPath(req.getFormPath()).setVersion("0")
        );
        return R.ok(save);
    }

    /**
     * 部署流程
     */
    @ResponseBody
    @PostMapping("deploy")
    public R deploy(@Valid FlowDeployReq req) throws IOException {
        Definition definition = null;
        if (req.getType() == Type.JSON) {
            definition = defService.importJson(req.getJson());

        } else if (req.getType() == Type.JSONFILE) {
            definition = defService.importIs(req.getFile().getInputStream());
        }
        if (definition == null) {
            return R.failed("流程定义导入失败");
        }
        return R.ok(definition.getId());
    }

    /**
     * 发布流程
     */
    @ResponseBody
    @PostMapping("publish")
    public R publish(Long id) {
        return R.ok(defService.publish(id));
    }

    /**
     * 取消发布流程
     */
    @ResponseBody
    @PostMapping("unpublish")
    public R unpublish(Long id) {
        return R.ok(defService.unPublish(id));
    }

    /**
     * 复制流程
     */
    @ResponseBody
    @PostMapping("copy")
    public R copy(Long id) {
        return R.ok(defService.copyDef(id));
    }

    /**
     * 激活流程
     */
    @ResponseBody
    @PostMapping("active")
    public R active(Long id) {
        return R.ok(defService.active(id));
    }

    /**
     * 取消激活流程
     */
    @ResponseBody
    @PostMapping("unactive")
    public R unactive(Long id) {
        return R.ok(defService.unActive(id));
    }

    /**
     * 删除流程
     */
    @ResponseBody
    @PostMapping("delete")
    public R delete(Long id) {
        return R.ok(defService.removeDef(Collections.singletonList(id)));
    }

    /**
     * * 查询流程定义
     */
    @ResponseBody
    @GetMapping("design")
    public R query(Long id) {
        return R.ok(defService.queryDesign(id));
    }

    /**
     * * 查询流程定义
     */
    @ResponseBody
    @GetMapping("definition")
    public R definition(Long id) {
        return R.ok(defService.getAllDataDefinition(id));
    }


}
