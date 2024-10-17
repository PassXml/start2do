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
import org.start2do.dto.req.dict.item.DictItemAddReq;
import org.start2do.dto.req.dict.item.DictItemUpdateReq;
import org.start2do.dto.resp.dict.item.DictItemDetailResp;
import org.start2do.dto.resp.dict.item.DictItemPageResp;
import org.start2do.entity.business.SysDictItem;
import org.start2do.entity.business.query.QSysDictItem;
import org.start2do.service.servlet.SysDictItemService;
import org.start2do.util.BeanValidatorUtil;

/**
 * 字典-子项管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict/item")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysDictItemController {

    private final SysDictItemService sysDictItemService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<DictItemPageResp>> page(Page page, IdStrReq req) {
        BeanValidatorUtil.validate(req);
        QSysDictItem qClass = new QSysDictItem().dictId.eq(req.getId());
        return R.ok(sysDictItemService.page(qClass, page, DictDtoMapper.INSTANCE::toDictPageItemResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody DictItemAddReq req) {
        BeanValidatorUtil.validate(req);
        SysDictItem item = DictDtoMapper.INSTANCE.toDictItem(req);
        sysDictItemService.save(item);
        return R.ok();
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody DictItemUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysDictItem item = sysDictItemService.getById(req.getId());
        DictDtoMapper.INSTANCE.dictItemUpdate(item, req);
        sysDictItemService.update(item);
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<DictItemDetailResp> detail(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        SysDictItem item = sysDictItemService.getById(req.getId());
        return R.ok(DictDtoMapper.INSTANCE.toDictItemDetailResp(item));

    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        sysDictItemService.deleteById(req.getId());
        return R.ok();
    }
}
