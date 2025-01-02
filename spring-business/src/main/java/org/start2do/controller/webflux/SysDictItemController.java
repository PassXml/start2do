package org.start2do.controller.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.dto.mapper.DictDtoMapper;
import org.start2do.dto.req.dict.item.DictItemAddReq;
import org.start2do.dto.req.dict.item.DictItemUpdateReq;
import org.start2do.dto.resp.dict.item.DictItemDetailResp;
import org.start2do.dto.resp.dict.item.DictItemPageResp;
import org.start2do.entity.business.SysDictItem;
import org.start2do.entity.business.query.QSysDictItem;
import org.start2do.service.webflux.SysDictItemReactiveService;
import org.start2do.util.BeanValidatorUtil;
import reactor.core.publisher.Mono;

/**
 * 字典-子项管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict/item")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "dict", havingValue = "true",matchIfMissing = true)
public class SysDictItemController {

    private final SysDictItemReactiveService sysDictItemService;

    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<DictItemPageResp>>> page(Page page, IdStrReq req) {
        BeanValidatorUtil.validate(req);
        QSysDictItem qClass = new QSysDictItem().dictId.eq(req.getId());
        return sysDictItemService.pageReactive(qClass, page, DictDtoMapper.INSTANCE::toDictPageItemResp).map(R::ok);
    }

    /**
     * 添加
     */
    @SysLogSetting("添加字典子项目")
    @PostMapping("add")
    public Mono<R<Boolean>> add(@RequestBody DictItemAddReq req) {
        BeanValidatorUtil.validate(req);
        SysDictItem item = DictDtoMapper.INSTANCE.toDictItem(req);
        return sysDictItemService.saveReactive(item).map(item1 -> true).map(R::ok);
    }

    /**
     * 更新
     */
    @SysLogSetting("更新字典子项目")
    @PostMapping("update")
    public Mono<R<Boolean>> update(@RequestBody DictItemUpdateReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictItemService.getByIdReactive(req.getId()).map(item -> {
            DictDtoMapper.INSTANCE.dictItemUpdate(item, req);
            return item;
        }).flatMap(sysDictItemService::updateReactive).map(item -> true).map(R::ok);
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<DictItemDetailResp>> detail(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictItemService.getByIdReactive(req.getId()).map(DictDtoMapper.INSTANCE::toDictItemDetailResp)
            .map(R::ok);
    }

    /**
     * 删除
     */
    @SysLogSetting("删除字典子项目")
    @GetMapping("delete")
    public Mono<R<Boolean>> delete(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictItemService.deleteByIdReactive(req.getId()).map(R::ok);
    }
}
