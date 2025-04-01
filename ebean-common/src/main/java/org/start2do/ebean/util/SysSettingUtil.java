package org.start2do.ebean.util;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;
import org.start2do.util.StringUtils;

@Slf4j
public class SysSettingUtil {

    private final SysSettingService sysSettingService;


    @Getter
    private static SysSettingUtil sysSettingUtil;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashMap;

    public SysSettingUtil(SysSettingService sysSettingService) {
        this.sysSettingService = sysSettingService;
        hashMap = new ConcurrentHashMap<>();
        SysSettingUtil.sysSettingUtil = this;
    }


    public static String getLabel(String type, String key) {
        if (StringUtils.isEmpty(type)) {
            return type;
        }
        if (StringUtils.isEmpty(key)) {
            return key;
        }
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil).map(t -> t.hashMap.get(type)).map(
            t -> t.get(key)
        ).orElseGet(() -> key);
    }

    public static ConcurrentHashMap<String, String> getItems(String type) {
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil)
            .map(e -> e.hashMap.get(type)).orElse(new ConcurrentHashMap<>());
    }


    @Scheduled(cron = "0 0/10 0 * * ?")
    public void sync() {
        if (sysSettingService == null) {
            log.warn("需要注入SysSettingService");
            return;
        }
        hashMap.clear();
        for (SysSetting dto : sysSettingService.findAll(new QSysSetting().enable.eq(EnableType.Enable))) {
            if (dto.getType() == null) {
                continue;
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
