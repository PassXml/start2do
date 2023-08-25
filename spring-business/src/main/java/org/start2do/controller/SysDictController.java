package org.start2do.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import org.start2do.service.SysDictItemService;
import org.start2do.service.SysDictService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.ListUtil;

/**
 * 字典管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict")
public class SysDictController {

    private final SysDictService sysDictService;
    private final SysDictItemService sysDictItemService;

    /**
     * 所有字典值
     */
    @GetMapping("all")
    public R<List<DictAllResp>> all() {
        List<DictAllResp> collect = sysDictService.findAll().stream().map(DictDtoMapper.INSTANCE::toDictAllResp)
            .collect(Collectors.toList());
//        MenuResp
        List<SysDictItem> items = sysDictItemService.findAll();
        ListUtil.fillIn(collect, items,
            (dictAllResp, sysDictItem) -> dictAllResp.getId().equals(sysDictItem.getDictId()),
            (dictAllResp, sysDictItem) -> {
                dictAllResp.setItems(
                    sysDictItem.stream().map(DictDtoMapper.INSTANCE::toDictPageItemResp).collect(Collectors.toList()));
            });
        Map<String, DictAllResp> add = new HashMap<>();

        for (IDictItem iDictItem : StaticDictPool.get().keySet()) {
            String className = iDictItem.getClass().getName();
            boolean hasAdd = true;
            for (DictAllResp resp : collect) {
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
                resp.getItems().add(new DictItemPageResp(
                    iDictItem.getLabel(),
                    iDictItem.getValue(),
                    0
                ));
                add.put(className, resp);
            }
        }
        collect.addAll(add.values());
        return R.ok(collect);
    }

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
    public R delete(UUIDReq req) {
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
    public R<DictDetailResp> detail(UUIDReq req) {
        BeanValidatorUtil.validate(req);
        SysDict dict = sysDictService.getById(req.getId());
        return R.ok(DictDtoMapper.INSTANCE.toDictDetailResp(dict));
    }
}
