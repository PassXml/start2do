package org.start2do.ebean.util;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;

@RequiredArgsConstructor
@Component
public class SysSettingUtil {

    private final SysSettingService sysSettingService;

    @Getter
    private static SysSettingUtil sysSettingUtil;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashMap;

    @PostConstruct
    public void init() {
        hashMap = new ConcurrentHashMap<>();
        SysSettingUtil.sysSettingUtil = this;
    }


    public static String getLabel(String type, String key) {
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil.hashMap.get(type))
            .map(stringStringHashMap -> stringStringHashMap.get(key)).orElse(key);
    }

    public static ConcurrentHashMap<String, String> getItems(String type) {
        return SysSettingUtil.sysSettingUtil.hashMap.getOrDefault(type, new ConcurrentHashMap<>());
    }


    @Scheduled(cron = "0 0/10 0 * * ?")
    public void sync() {
        if (hashMap == null) {
            init();
        }
        for (SysSetting dto : sysSettingService.findAll(new QSysSetting().enable.eq(EnableType.Enable))) {
            if (dto.getType() == null) {
                return;
            }
            ConcurrentHashMap<String, String> map = SysSettingUtil.sysSettingUtil.hashMap.get(dto.getType());
            if (map == null) {
                map = new ConcurrentHashMap<>();
            }
            map.put(dto.getKey(), dto.getValue());
            SysSettingUtil.sysSettingUtil.hashMap.put(dto.getType(), map);
        }
    }


}