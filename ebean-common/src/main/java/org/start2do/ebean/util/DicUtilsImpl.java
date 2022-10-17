package org.start2do.ebean.util;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;
import org.start2do.util.DicUtils;

@RequiredArgsConstructor
public class DicUtilsImpl extends DicUtils {

    protected final SysSettingService sysSettingService;

    private void set(SysSetting dto) {
        HashMap<String, String> map = DicUtils.hashMap.get(dto.getType());
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(dto.getKey(), dto.getValue());
        DicUtils.hashMap.put(dto.getType(), map);
    }

    @Scheduled(cron = "0 0/10 0 * * ?")
    public void sync() {
        for (SysSetting dto : sysSettingService.findAll()) {
            set(dto);
        }
    }

    @PostConstruct
    public void init() {
        sync();
        DicUtils.GETFUNCTION2 = (type, key) -> {
            String result = null;
            for (SysSetting setting : sysSettingService.findAll(
                new QSysSetting().enable.eq(EnableType.Enable).type.eq(type))) {
                set(setting);
                if (key.equals(setting.getKey())) {
                    result = setting.getValue();
                }
            }
            return result;
        };
        DicUtils.GETFUNCTION = (type) -> {
            for (SysSetting setting : sysSettingService.findAll(
                new QSysSetting().enable.eq(EnableType.Enable).type.eq(type))) {
                set(setting);
            }
            return DicUtils.hashMap.get(type);
        };
    }


}
