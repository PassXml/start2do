package org.start2do;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationListener;
import org.start2do.ebean.util.SysSettingUtil;

@Slf4j
public class EbeanSpringInitListener implements ApplicationListener<AvailabilityChangeEvent> {


    @Override
    public void onApplicationEvent(AvailabilityChangeEvent event) {
        if (ReadinessState.ACCEPTING_TRAFFIC == event.getState()) {
            try {
                SysSettingUtil util = SysSettingUtil.getSysSettingUtil();
                if (util != null) {
                    // 应用就绪后再进行业务配置初始化和缓存同步，确保 DataSource / Ebean 已准备完成
                    util.init();
                    util.sync();
                }
            } catch (Throwable e) {
                log.error("应用就绪阶段业务配置初始化或同步失败", e);
            }
        }
    }
}
