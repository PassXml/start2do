package org.start2do.util.spring;

import java.util.concurrent.ForkJoinPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 等待完成初始化之后执行初始化
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class SpringInitListenerUtil implements ApplicationListener<AvailabilityChangeEvent> {

    @Override
    public void onApplicationEvent(AvailabilityChangeEvent event) {
        if (ReadinessState.ACCEPTING_TRAFFIC == event.getState()) {
            try {
                if (SpringBeanUtil.getContext() == null) {
                    return;
                }
                SpringBeanUtil.getBeans(WaitInitCompleteRunner.class).forEach((s, initRunner) -> {
                    ForkJoinPool.commonPool().submit(() -> {
                        try {
                            log.info("运行:{}", s);
                            initRunner.init();
                        } catch (Exception e) {
                            log.error("{},{}", s, e.getMessage(), e);
                        }
                    });
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public interface WaitInitCompleteRunner {

        void init();
    }
}
