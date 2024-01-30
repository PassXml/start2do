package org.start2do.controller.webflux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.UUIDReq;
import org.start2do.dto.mapper.DictDtoMapper;
import org.start2do.dto.req.dict.DictAddReq;
import org.start2do.dto.req.dict.DictPageReq;
import org.start2do.dto.req.dict.DictUpdateReq;
import org.start2do.dto.resp.dict.DictAllResp;
import org.start2do.dto.resp.dict.DictDetailResp;
import org.start2do.dto.resp.dict.DictPageResp;
import org.start2do.dto.resp.dict.item.DictItemPageResp;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.ebean.dict.StaticDictPool;
import org.start2do.ebean.util.Where;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.SysDictItem;
import org.start2do.entity.business.query.QSysDict;
import org.start2do.service.webflux.SysDictItemReactiveService;
import org.start2do.service.webflux.SysDictReactiveService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.ListUtil;
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
    private final SysDictItemReactiveService sysDictItemService;

    /**
     * 所有字典值
     */
    @GetMapping("all")
    public Mono<R<List<DictAllResp>>> all() {
        return Mono.from(sysDictService.findAll())
            .map(sysDicts -> sysDicts.stream().map(DictDtoMapper.INSTANCE::toDictAllResp).collect(Collectors.toList()))
            .zipWhen(dictAllResps -> sysDictItemService.findAll()).map(objects -> {
                    List<SysDictItem> items = objects.getT2();
                    List<DictAllResp> resps = objects.getT1();
                    ListUtil.fillIn(resps, items,
                        (dictAllResp, sysDictItem) -> dictAllResp.getId().equals(sysDictItem.getDictId()),
                        (dictAllResp, sysDictItem) -> {
                            dictAllResp.setItems(sysDictItem.stream().map(DictDtoMapper.INSTANCE::toDictPageItemResp)
                                .collect(Collectors.toList()));
                        });
                    Map<String, DictAllResp> add = new HashMap<>();

                    for (IDictItem iDictItem : StaticDictPool.get().keySet()) {
                        String className = iDictItem.getClass().getName();
                        boolean hasAdd = true;
                        for (DictAllResp resp : resps) {
                            if (className.equals(resp.getDictName())) {
                                hasAdd = false;
                                boolean hasAdd2 = true;
                                List<DictItemPageResp> addItem = new ArrayList<>();
                                for (DictItemPageResp item : resp.getItems()) {
                                    if (iDictItem.getValue().equals(item.getItemData())) {
                                        hasAdd2 = false;
                                    }
                                    if (hasAdd2) {
                                        addItem.add(new DictItemPageResp(iDictItem.getLabel(), iDictItem.getValue(), 0));
                                    }
                                }
                            }
                        }
                        if (hasAdd) {
                            DictAllResp resp = add.get(className);
                            if (resp == null) {
                                resp = new DictAllResp(className, new ArrayList<>());
                            }
                            resp.getItems().add(new DictItemPageResp(iDictItem.getLabel(), iDictItem.getValue(), 0));
                            add.put(className, resp);
                        }
                    }
                    resps.addAll(add.values());
                    return resps;
                }
            ).map(R::ok);
    }

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
    public Mono<R<Boolean>> delete(UUIDReq req) {
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
    public Mono<R<DictDetailResp>> detail(UUIDReq req) {
        BeanValidatorUtil.validate(req);
        return sysDictService.getById(req.getId()).map(DictDtoMapper.INSTANCE::toDictDetailResp).map(R::ok);
    }
}
