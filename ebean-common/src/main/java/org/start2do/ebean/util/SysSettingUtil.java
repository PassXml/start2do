package org.start2do.ebean.util;

import io.ebean.Transaction;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;
import org.start2do.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(DataSource.class)
@Component
@ConditionalOnProperty(
    prefix = "start2do.ebean",
    name = {"enable", "enable-setting-service"},
    havingValue = "true",
    matchIfMissing = true)
public class SysSettingUtil
    implements EntityHook<SysSetting>, ApplicationListener<AvailabilityChangeEvent> {

    private final SysSettingService sysSettingService;
    @Getter
    private static SysSettingUtil sysSettingUtil;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashMap;


    public static String getLabel(String type, String key, String defaultValue) {
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(key)) {
            return defaultValue;
        }
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil).map(t -> t.hashMap).map(map -> map.get(type))
            .map(t -> t.get(key)).orElseGet(() -> defaultValue);
    }

    public static String getLabel(String type, String key) {
        if (StringUtils.isEmpty(type)) {
            return type;
        }
        if (StringUtils.isEmpty(key)) {
            return key;
        }
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil).map(t -> t.hashMap).map(map -> map.get(type))
            .map(t -> t.get(key)).orElseGet(() -> key);
    }

    public static ConcurrentHashMap<String, String> getItems(String type) {
        return SysSettingUtil.sysSettingUtil.hashMap.getOrDefault(type, new ConcurrentHashMap<>());
    }



    @Scheduled(cron = "0 0/10 0 * * ?")
    public void sync() {
        if (sysSettingService == null) {
            log.warn("需要注入SysSettingService");
            return;
        }
        hashMap.clear();
        try {
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
        } catch (Exception e) {
            log.error("读取系统设置表失败：{}", e.getMessage());
        }
    }

  @Override
  public Class<SysSetting> getKey() {
    return SysSetting.class;
  }

  @Override
  public void insertAfter(SysSetting obj, Transaction transaction) {
    sync();
  }

  @Override
  public void updateAfter(SysSetting obj, Transaction transaction) {
    sync();
  }

  @Override
  public void postSoftDelete(SysSetting obj, Transaction transaction) {
    sync();
  }

  @Override
  public void postDelete(SysSetting obj, Transaction transaction) {
    sync();
  }

  @Override
  public void onApplicationEvent(AvailabilityChangeEvent event) {
    hashMap = new ConcurrentHashMap<>();
    SysSettingUtil.sysSettingUtil = this;
    if (ReadinessState.ACCEPTING_TRAFFIC == event.getState()) {
      try {
        sync();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }
}
