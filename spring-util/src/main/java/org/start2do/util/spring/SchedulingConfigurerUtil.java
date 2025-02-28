package org.start2do.util.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "start2do.util.schedulingEnable", value = "enable", havingValue = "true")
public class SchedulingConfigurerUtil implements SchedulingConfigurer {

    @Getter
    private static SchedulingConfigurerUtil schedulingConfigurerUtil;
    @Getter
    private ScheduledTaskRegistrar taskRegistrar;
    private Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @PostConstruct
    public void init() {
        SchedulingConfigurerUtil.schedulingConfigurerUtil = this;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.taskRegistrar = taskRegistrar;
    }

    /**
     * 添加一个带ID的定时任务
     *
     * @param taskId   任务的唯一标识符
     * @param runnable 要执行的任务
     * @param cron     cron表达式
     */
    public static void addCronTask(String taskId, Runnable runnable, String cron) {
        // 如果已存在同ID的任务，先取消它
        cancelTask(taskId);
        ScheduledFuture<?> future = schedulingConfigurerUtil.taskRegistrar.getScheduler()
            .schedule(runnable, new CronTrigger(cron));
        schedulingConfigurerUtil.scheduledTasks.put(taskId, future);
    }

    /**
     * 取消指定ID的任务
     *
     * @param taskId 任务ID
     * @return 是否成功取消任务
     */
    public static boolean cancelTask(String taskId) {
        ScheduledFuture<?> future = schedulingConfigurerUtil.scheduledTasks.get(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            schedulingConfigurerUtil.scheduledTasks.remove(taskId);
            return cancelled;
        }
        return false;
    }

    /**
     * 检查任务是否存在
     *
     * @param taskId 任务ID
     * @return 任务是否存在
     */
    public static boolean hasTask(String taskId) {
        return schedulingConfigurerUtil.scheduledTasks.containsKey(taskId);
    }

    /**
     * 获取所有任务ID
     *
     * @return 所有任务ID的集合
     */
    public static java.util.Set<String> getAllTaskIds() {
        return schedulingConfigurerUtil.scheduledTasks.keySet();
    }
}
