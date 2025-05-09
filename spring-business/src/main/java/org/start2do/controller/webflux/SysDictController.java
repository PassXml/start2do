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
import org.start2do.dto.BusinessException;
import org.start2do.dto.IdStrReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.annotation.SysLogSetting;
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
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "dict", havingValue = "true", matchIfMissing = true)
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
        return sysDictService.pageReactive(qClass, page, DictDtoMapper.INSTANCE::toDictPageResp).map(R::ok);
    }

    /**
     * 删除
     */
    @SysLogSetting("删除字典")
    @GetMapping("delete")
    public Mono<R<Boolean>> delete(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        SysDict dict = sysDictService.findOneById(req.getId());
        if (dict.getDictType() == SysDict.Type.SYSTEM) {
            throw new BusinessException("系统内置无法修改");
        }
        return sysDictService.remove(req.getId()).map(R::ok);
    }

    /**
     * 添加
     */
    @SysLogSetting("添加字典")
    @PostMapping("add")
    public Mono<R<Boolean>> add(@RequestBody DictAddReq req) {
        BeanValidatorUtil.validate(req);
        if (sysDictService.exists(new QSysDict().dictKey.eq(req.getDictName()))) {
            throw new BusinessException("已被使用");
        }
        SysDict sysDict = DictDtoMapper.INSTANCE.toSysDict(req);
        return sysDictService.saveReactive(sysDict).map(dict -> true).map(R::ok);
    }

    /**
     * 更新
     */
    @SysLogSetting("更新字典")
    @PostMapping("update")
    public Mono<R<Boolean>> update(@RequestBody DictUpdateReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictService.getByIdReactive(req.getId()).map(dict -> {
            if (!dict.getDictName().equals(req.getDictName())) {
                if (sysDictService.exists(new QSysDict().dictKey.eq(req.getDictName()))) {
                    throw new BusinessException("已被使用");
                }
            }
            DictDtoMapper.INSTANCE.updateSysDict(dict, req);
            return dict;
        }).flatMap(sysDictService::updateReactive).map(dict -> true).map(R::ok);
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<DictDetailResp>> detail(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictService.getByIdReactive(req.getId()).map(DictDtoMapper.INSTANCE::toDictDetailResp).map(R::ok);
    }
}
