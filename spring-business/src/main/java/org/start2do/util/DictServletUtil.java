package org.start2do.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.start2do.entity.business.SysDict;
import org.start2do.entity.business.SysDictItem;
import org.start2do.service.servlet.SysDictItemService;
import org.start2do.service.servlet.SysDictService;

@Component
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.SERVLET)
public class DictServletUtil {

    private final SysDictService DICT_SERVICE;
    private final SysDictItemService SYS_DICT_ITEM_SERVICE;
    private ConcurrentMap<String, ConcurrentHashMap<String, String>> concurrentMap;
    @Getter
    private static DictServletUtil dictUtil;

    public static String getLabel(String type, String key) {
        return Optional.ofNullable(dictUtil.concurrentMap.get(type))
            .map(stringStringHashMap -> stringStringHashMap.get(key)).orElse(key);
    }

    public static ConcurrentHashMap<String, String> getItems(String type) {
        return dictUtil.concurrentMap.getOrDefault(type, new ConcurrentHashMap<>());
    }


    @Scheduled(cron = "0 0/10 * * * ?")
    public void sync() {
        if (concurrentMap == null) {
            init();
        }
        concurrentMap.clear();
        List<SysDict> dicts = DICT_SERVICE.findAll();
        List<SysDictItem> items = SYS_DICT_ITEM_SERVICE.findAll();
        Map<UUID, List<SysDictItem>> map = items.stream().collect(Collectors.groupingBy(SysDictItem::getDictId));
        for (SysDict dict : dicts) {
            ConcurrentHashMap<String, String> itemMap = new ConcurrentHashMap<>();
            List<SysDictItem> itemList = map.get(dict.getId());
            if (itemList == null) {
                continue;
            }
            for (SysDictItem item : itemList) {
                itemMap.put(item.getItemData(), item.getItemName());
            }
            concurrentMap.put(dict.getDictName(), itemMap);
        }
    }


    @PostConstruct
    public void init() {
        DictServletUtil.dictUtil = this;
        concurrentMap = new ConcurrentHashMap<>();
    }

}
