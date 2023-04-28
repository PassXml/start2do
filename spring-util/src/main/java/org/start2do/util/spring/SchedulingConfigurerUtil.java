package org.start2do.util.spring;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(prefix = "start2do.util.schedulingEnable", value = "enable", havingValue = "true")
public class SchedulingConfigurerUtil implements SchedulingConfigurer {

    @Getter
    private static SchedulingConfigurerUtil schedulingConfigurerUtil;
    @Getter
    private ScheduledTaskRegistrar taskRegistrar;

    @PostConstruct
    public void init() {
        SchedulingConfigurerUtil.schedulingConfigurerUtil = this;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.taskRegistrar = taskRegistrar;
    }

    public static void addCronTask(Runnable runnable, String cron) {
        schedulingConfigurerUtil.taskRegistrar.addCronTask(runnable, cron);
    }
}
