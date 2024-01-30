package org.start2do.util.spring;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.start2do.util.ThreadUtil.CustomForkJoinWorkerThreadFactory;

/**
 * 等待完成初始化之后执行初始化
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class SpringInitListenerUtil implements ApplicationListener<AvailabilityChangeEvent> {

    private ForkJoinPool pool = new ForkJoinPool(10, new CustomForkJoinWorkerThreadFactory("System-Init"), (t, e) -> {
        log.error("线程异常:{}", t.getName(), e);
    }, true);

    @Override
    public void onApplicationEvent(AvailabilityChangeEvent event) {
        if (ReadinessState.ACCEPTING_TRAFFIC == event.getState()) {
            try {
                if (SpringBeanUtil.getContext() == null) {
                    return;
                }
                Map<String, WaitInitCompleteRunner> beans = SpringBeanUtil.getBeans(WaitInitCompleteRunner.class);
                beans.forEach((s, initRunner) -> {
                    pool.submit(() -> {
                        try {
                            log.info("运行:{}", s);
                            initRunner.init();
                        } catch (Exception e) {
                            log.error("{},{}", s, e.getMessage(), e);
                        }
                    });
                });
                //关闭线程池
                pool.shutdown();
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                pool = null;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public interface WaitInitCompleteRunner {

        void init();
    }
}
