package org.start2do.ebean.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.start2do.BusinessSettingInitConfiguration;
import org.start2do.BusinessSettingInitConfiguration.SettingItem;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.SysSetting;
import org.start2do.ebean.entity.query.QSysSetting;
import org.start2do.ebean.service.SysSettingService;
import org.start2do.util.StringUtils;

@Slf4j
public class SysSettingUtil {

    private final SysSettingService sysSettingService;


    private final BusinessSettingInitConfiguration businessSettingInitConfiguration;
    @Getter
    private static SysSettingUtil sysSettingUtil;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashMap;

    public SysSettingUtil(SysSettingService sysSettingService,
        BusinessSettingInitConfiguration businessSettingInitConfiguration) {
        this.sysSettingService = sysSettingService;
        this.businessSettingInitConfiguration = businessSettingInitConfiguration;
        hashMap = new ConcurrentHashMap<>();
        SysSettingUtil.sysSettingUtil = this;
    }

    /**
     * 使用配置文件中的初始化项进行初始化
     * 注意：该方法不再在 Bean PostConstruct 阶段调用，而是由应用就绪事件触发，
     * 避免在 DataSource / Ebean 尚未完全准备好时访问数据库。
     */
    public void init() {
        if (businessSettingInitConfiguration == null) {
            return;
        }
        try {
            init(businessSettingInitConfiguration.getSettings());
        } catch (Exception e) {
            // 启动阶段避免因为初始化失败导致应用不可用，错误记录后由后续任务或人工干预处理
            log.error("业务配置初始化失败，将在后续手动或定时任务中重试", e);
        }
    }

    /**
     * 支持外部传入初始化配置列表进行初始化
     *
     * @param settings 业务配置初始化项列表
     */
    public void init(List<SettingItem> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }
        // 为了兼容第三方在应用早期调用 init(List<SettingItem>) 的场景，这里增加简单的重试机制，
        // 以应对 DataSource / Ebean 尚未完全初始化导致的短暂失败；参数可通过配置覆盖。
        int maxRetry = Optional.ofNullable(businessSettingInitConfiguration)
            .map(BusinessSettingInitConfiguration::getRetryTimes)
            .filter(v -> v != null && v > 0)
            .orElse(3);
        long sleepMs = Optional.ofNullable(businessSettingInitConfiguration)
            .map(BusinessSettingInitConfiguration::getRetryIntervalMs)
            .filter(v -> v != null && v > 0)
            .orElse(2000L);
        for (int i = 1; i <= maxRetry; i++) {
            try {
                doInit(settings);
                return;
            } catch (Exception e) {
                if (i == maxRetry) {
                    // 最后一轮仍然失败，抛出异常给调用方处理
                    log.error("业务配置初始化失败，已重试 {} 次仍然失败", maxRetry, e);
                    throw e;
                }
                log.warn("业务配置初始化失败，可能是数据库尚未准备好，第 {} 次重试将在 {} ms 后进行", i, sleepMs);
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    // 中断时不再继续重试，直接退出
                    return;
                }
            }
        }
    }

    /**
     * 实际的初始化逻辑：查询已存在的配置并插入缺失项
     */
    private void doInit(List<SettingItem> settings) {
        List<String> keys = settings.stream().map(SettingItem::getKey)
            .collect(Collectors.toList());
        Set<String> set = sysSettingService.findAll(new QSysSetting().key.in(keys)).stream().map(SysSetting::getKey)
            .collect(Collectors.toSet());
        for (SettingItem setting : settings) {
            if (set.contains(setting.getKey())) {
                continue;
            }
            sysSettingService.save(
                new SysSetting(setting.getEnable() ? EnableType.Enable : EnableType.DisEnable, setting.getRemark(),
                    setting.getType(), setting.getKey(), setting.getValue()));
        }
    }


    public static String getLabel(String type, String key) {
        if (StringUtils.isEmpty(type)) {
            return type;
        }
        if (StringUtils.isEmpty(key)) {
            return key;
        }
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil).map(t -> t.hashMap.get(type)).map(t -> t.get(key))
            .orElseGet(() -> key);
    }

    public static String getLabel(String type, String key, String defaultValue) {
        if (StringUtils.isEmpty(type)) {
            return defaultValue;
        }
        if (StringUtils.isEmpty(key)) {
            return defaultValue;
        }
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil).map(t -> t.hashMap.get(type)).map(t -> t.get(key))
            .orElseGet(() -> defaultValue);
    }

    public static ConcurrentHashMap<String, String> getItems(String type) {
        return Optional.ofNullable(SysSettingUtil.sysSettingUtil).map(e -> e.hashMap.get(type))
            .orElse(new ConcurrentHashMap<>());
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
