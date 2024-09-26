package org.start2do.controller.webflux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.dto.mapper.DictDtoMapper;
import org.start2do.dto.resp.dict.DictAllResp;
import org.start2do.dto.resp.dict.item.DictItemPageResp;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.ebean.dict.StaticDictPool;
import org.start2do.entity.business.SysDictItem;
import org.start2do.service.webflux.SysDictItemReactiveService;
import org.start2do.service.webflux.SysDictReactiveService;
import org.start2do.util.ListUtil;
import reactor.core.publisher.Mono;

/**
 * 字典管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "custom-dict", havingValue = "false")
public class SysDictCustomController {

    private final SysDictReactiveService sysDictService;
    private final SysDictItemReactiveService sysDictItemService;

    /**
     * 所有字典值
     */
    @GetMapping("all")
    public Mono<R<List<DictAllResp>>> all() {
        return Mono.from(sysDictService.findAll())
            .map(sysDicts -> sysDicts.stream().map(DictDtoMapper.INSTANCE::toDictAllResp).toList())
            .zipWhen(dictAllResps -> sysDictItemService.findAll()).map(objects -> {
                List<SysDictItem> items = objects.getT2();
                List<DictAllResp> resps = objects.getT1();
                ListUtil.fillIn(resps, items,
                    (dictAllResp, sysDictItem) -> dictAllResp.getId().equals(sysDictItem.getDictId()),
                    (dictAllResp, sysDictItem) -> {
                        dictAllResp.setItems(
                            sysDictItem.stream().map(DictDtoMapper.INSTANCE::toDictPageItemResp).toList());
                    });
                return getDictAllResps(resps);
            }).map(R::ok);
    }

    private List<DictAllResp> getDictAllResps(List<DictAllResp> resps) {
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
                    resp = new DictAllResp(className,iDictItem.getDesc(), new ArrayList<>());
                }
                resp.getItems().add(new DictItemPageResp(iDictItem.getLabel(), iDictItem.getValue(), 0));
                add.put(className, resp);
            }
        }
        ArrayList<DictAllResp> list = new ArrayList<>(add.values());
        list.addAll(resps);
        return list;
    }

}
