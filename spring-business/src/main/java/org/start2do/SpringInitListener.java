package org.start2do;

import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.start2do.util.DictServletUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SpringInitListener implements ApplicationListener<AvailabilityChangeEvent> {

  @Override
  public void onApplicationEvent(AvailabilityChangeEvent event) {
    if (ReadinessState.ACCEPTING_TRAFFIC == event.getState()) {
      if (DictServletUtil.getDictUtil() != null) {
        try {
          DictServletUtil.getDictUtil().sync();
          log.info("DictServletUtil sync completed successfully.");
        } catch (PersistenceException e) {
          log.error("Error during DictServletUtil sync: {}", e.getMessage());
        }
      } else {
        log.warn("DictServletUtil is null, starting background thread for retry...");
        // 使用单线程线程池执行重试逻辑
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(
            () -> {
              int maxRetries = 3;
              int retryCount = 0;
              long delayMillis = 10000; // 10秒延时

              while (retryCount < maxRetries) {
                if (DictServletUtil.getDictUtil() != null) {
                  try {
                    DictServletUtil.getDictUtil().sync();
                    log.info("DictServletUtil sync completed successfully in background thread.");
                    break; // 成功后退出循环
                  } catch (PersistenceException e) {
                    log.error(
                        "Error during DictServletUtil sync in background thread: {}",
                        e.getMessage());
                    break; // 异常后退出循环，不再重试
                  }
                } else {
                  retryCount++;
                  log.warn(
                      "DictServletUtil is null, retrying {}/{} after {}ms delay in background thread...",
                      retryCount,
                      maxRetries,
                      delayMillis);
                  try {
                    Thread.sleep(delayMillis); // 延时10秒
                  } catch (InterruptedException e) {
                    log.error("Interrupted during delay in background thread: {}", e.getMessage());
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    break; // 中断后退出循环
                  }
                }
              }
              if (retryCount == maxRetries && DictServletUtil.getDictUtil() == null) {
                log.error(
                    "Failed to initialize DictServletUtil after {} retries in background thread.",
                    maxRetries);
              }
            });
        executor.shutdown(); // 提交任务后关闭线程池，不接受新任务
      }
    }
  }
}
