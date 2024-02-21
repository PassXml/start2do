package org.start2do;

import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationListener;
import org.start2do.util.DictServletUtil;

@Slf4j
public class SpringInitListener implements ApplicationListener<AvailabilityChangeEvent> {

    @Override
    public void onApplicationEvent(AvailabilityChangeEvent event) {
        if (ReadinessState.ACCEPTING_TRAFFIC == event.getState()) {
            if (DictServletUtil.getDictUtil() != null) {
                try {
                    DictServletUtil.getDictUtil().sync();
                } catch (PersistenceException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
