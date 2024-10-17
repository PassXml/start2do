package org.start2do.controller.servlet;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.IdStrReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.DictDtoMapper;
import org.start2do.dto.req.dict.DictAddReq;
import org.start2do.dto.req.dict.DictPageReq;
import org.start2do.dto.req.dict.DictUpdateReq;
import org.start2do.dto.resp.dict.DictDetailResp;
import org.start2do.dto.resp.dict.DictPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.query.QSysDict;
import org.start2do.service.servlet.SysDictService;
import org.start2do.util.BeanValidatorUtil;

/**
 * 字典管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysDictController {

    private final SysDictService sysDictService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<DictPageResp>> page(Page page, DictPageReq req) {
        QSysDict qClass = new QSysDict();
        Where.ready().like(req.getName(), qClass.dictName)
            .notEmpty(req.getType(), s -> qClass.dictType.eq(SysDict.Type.find(s)));
        return R.ok(sysDictService.page(qClass, page, DictDtoMapper.INSTANCE::toDictPageResp));
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        sysDictService.remove(req.getId());
        return R.ok();
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody DictAddReq req) {
        BeanValidatorUtil.validate(req);
        SysDict sysDict = DictDtoMapper.INSTANCE.toSysDict(req);
        sysDictService.save(sysDict);
        return R.ok();
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody DictUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysDict dict = sysDictService.getById(req.getId());
        DictDtoMapper.INSTANCE.updateSysDict(dict, req);
        sysDictService.update(dict);
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<DictDetailResp> detail(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        SysDict dict = sysDictService.getById(req.getId());
        return R.ok(DictDtoMapper.INSTANCE.toDictDetailResp(dict));
    }
}
