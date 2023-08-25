package org.start2do;

import javax.persistence.PersistenceException;
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
            if (SysSettingUtil.getSysSettingUtil() != null) {
                try {
                    SysSettingUtil.getSysSettingUtil().sync();
                } catch (PersistenceException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
