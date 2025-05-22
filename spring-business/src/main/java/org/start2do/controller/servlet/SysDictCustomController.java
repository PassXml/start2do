package org.start2do.controller.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.dto.mapper.DictDtoMapper;
import org.start2do.dto.req.dict.DictAllReq;
import org.start2do.dto.resp.dict.DictAllResp;
import org.start2do.dto.resp.dict.item.DictItemPageResp;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.ebean.dict.StaticDictPool;
import org.start2do.entity.business.SysDictItem;
import org.start2do.service.servlet.SysDictItemService;
import org.start2do.service.servlet.SysDictService;
import org.start2do.util.ListUtil;
import org.start2do.dto.Permission;

/**
 * 字典管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict")
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "custom-dict", havingValue = "true",matchIfMissing = true)
@Permission(groupName = "自定义字典管理")
public class SysDictCustomController {

    private final SysDictService sysDictService;
    private final SysDictItemService sysDictItemService;

    /**
     * 所有字典值
     */
    @GetMapping("all")
    public R<List<DictAllResp>> all(
        DictAllReq req) {
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
                if (className.equals(resp.getDictKey())) {
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
                    resp = new DictAllResp(className, className, new ArrayList<>());
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
}
