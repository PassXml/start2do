package org.start2do.util.spring;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class HashedWheelTimerUtil {

    private HashedWheelTimer hashedWheelTimer;
    private ConcurrentHashMap<String, Timeout> taskMap = new ConcurrentHashMap<>();

    static {
        ThreadFactory threadFactory = r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName("HashedWheelTimer-" + thread.getId());
            return thread;
        };
        hashedWheelTimer = new HashedWheelTimer(threadFactory, 250, TimeUnit.MILLISECONDS, 1024, true);
    }

    public TimeTaskResult addTask(String key, Run timerTask, long delay, TimeUnit timeUnit) {
        if (key == null || key.isEmpty()) {
            key = UUID.randomUUID().toString();
        }
        String finalKey = key;
        if (taskMap.containsKey(finalKey)) {
            log.warn("Task with key {} already exists, will be replaced.", finalKey);
            stop(finalKey);
        }
        Timeout timeout = hashedWheelTimer.newTimeout(t -> {
            try {
                timerTask.run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                taskMap.remove(finalKey);
            }
        }, delay, timeUnit);
        taskMap.put(key, timeout);
        return new TimeTaskResult(key, timeout);
    }

    public boolean stop(String key) {
        Timeout timeout = taskMap.get(key);
        if (timeout != null) {
            boolean canceled = timeout.cancel();
            if (canceled) {
                taskMap.remove(key);
            }
            return canceled;
        }
        return false;
    }

    public TimeTaskResult addTask(LocalDate date, LocalTime time, long delay, String key, Run timerTask) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        if (dateTime.isBefore(now)) {
            return null;
        }
        long millis = Duration.between(now, dateTime).toMillis() + delay;
        return addTask(key, timerTask, millis, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        hashedWheelTimer.stop();
        taskMap.clear();
    }

    public interface Run {

        void run();
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class TimeTaskResult {

        private String id;
        private Timeout timeOut;

        public TimeTaskResult(String id, Timeout timeOut) {
            this.id = id;
            this.timeOut = timeOut;
        }
    }
}
