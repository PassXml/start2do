package org.start2do.cep.source;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.legacy.SourceFunction;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.cep.config.FlinkConfig;
import org.start2do.cep.dto.Event;
import org.start2do.dto.R;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class HttpEventSource implements SourceFunction<Event> {


    private static final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    private final FlinkConfig.HttpConfig httpConfig;

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        if (!httpConfig.isEnabled()) {
            log.info("HTTP event source is disabled. The source will not start an HTTP server and will remain idle.");
            while (running) {
                Thread.sleep(1000L);
            }
            return;
        }
        // 启动HTTP服务器
        startHttpServer();

        // 持续从队列中获取事件
        while (running) {
            try {
                // 阻塞等待事件
                Event event = eventQueue.take();
                ctx.collect(event);
                log.info("收集到事件: {}", event);
            } catch (InterruptedException e) {
                log.warn("HTTP源被中断", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void cancel() {
        running = false;
    }

    private void startHttpServer() {
        // 在单独线程中启动Spring Boot应用
        new Thread(() -> {
            try {
                log.info("Starting HTTP server on port {} with context path '{}'", httpConfig.getPort(), httpConfig.getContextPath());
                new SpringApplicationBuilder(HttpEventSource.class)
                    .properties(
                        "server.port=" + httpConfig.getPort(),
                        "server.servlet.context-path=" + httpConfig.getContextPath()
                    )
                    .run();
            } catch (Exception e) {
                log.error("启动HTTP服务器失败", e);
            }
        }).start();
    }

    // HTTP接口：接收单个事件
    @PostMapping("/single")
    public R receiveEvent(@RequestBody Event event) {
        try {
            if (event.getTimestamp() == null) {
                event.setTimestamp(LocalDateTime.now());
            }
            eventQueue.offer(event);
            log.info("接收到HTTP事件: {}", event.getEventId());
            return R.ok();
        } catch (Exception e) {
            log.error("处理HTTP事件失败", e);
            return R.failed();
        }
    }

    // HTTP接口：批量接收事件
    @PostMapping("/batch")
    public R receiveBatchEvents(@RequestBody Event[] events) {
        try {
            int count = 0;
            for (Event event : events) {
                if (event.getTimestamp() == null) {
                    event.setTimestamp(LocalDateTime.now());
                }
                eventQueue.offer(event);
                count++;
            }
            log.info("批量接收到{}个HTTP事件", count);
            return R.ok(count);
        } catch (Exception e) {
            log.error("处理批量HTTP事件失败", e);
            return R.failed().setError(e.getMessage());
        }
    }

    // 健康检查接口
    @GetMapping("/health")
    public R health() {
        return R.ok(eventQueue.size());
    }
}
