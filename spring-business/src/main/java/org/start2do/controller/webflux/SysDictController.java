package org.start2do.controller.webflux;

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
import org.start2do.service.webflux.SysDictReactiveService;
import org.start2do.util.BeanValidatorUtil;
import reactor.core.publisher.Mono;

/**
 * 字典管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysDictController {

    private final SysDictReactiveService sysDictService;


    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<DictPageResp>>> page(Page page, DictPageReq req) {
        QSysDict qClass = new QSysDict();
        Where.ready().like(req.getName(), qClass.dictName)
            .notEmpty(req.getType(), s -> qClass.dictType.eq(SysDict.Type.find(s)));
        return sysDictService.page(qClass, page, DictDtoMapper.INSTANCE::toDictPageResp).map(R::ok);
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public Mono<R<Boolean>> delete(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictService.remove(req.getId()).map(R::ok);
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public Mono<R<Boolean>> add(@RequestBody DictAddReq req) {
        BeanValidatorUtil.validate(req);
        SysDict sysDict = DictDtoMapper.INSTANCE.toSysDict(req);
        return sysDictService.save(sysDict).map(dict -> true).map(R::ok);
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public Mono<R<Boolean>> update(@RequestBody DictUpdateReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictService.getById(req.getId()).map(dict -> {
            DictDtoMapper.INSTANCE.updateSysDict(dict, req);
            return dict;
        }).flatMap(dict -> sysDictService.update(dict)).map(dict -> true).map(R::ok);
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<DictDetailResp>> detail(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictService.getById(req.getId()).map(DictDtoMapper.INSTANCE::toDictDetailResp).map(R::ok);
    }
}
