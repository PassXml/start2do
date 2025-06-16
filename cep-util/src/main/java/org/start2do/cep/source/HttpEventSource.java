package org.start2do.cep.source;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.legacy.SourceFunction;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.start2do.cep.config.FlinkConfig;
import org.start2do.cep.dto.Event;

@Slf4j
@RequiredArgsConstructor
public class HttpEventSource implements SourceFunction<Event> {


    private static final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    private final FlinkConfig.HttpConfig httpConfig;

    /**
     * 向事件队列中添加一个事件。
     *
     * @param event 要添加的事件
     */
    public static void offerEvent(Event event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        eventQueue.offer(event);
    }

    /**
     * 获取当前事件队列的大小。
     *
     * @return 队列中的事件数量
     */
    public static int getQueueSize() {
        return eventQueue.size();
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
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
                log.info("Starting HTTP server on port {} with context path '{}'", httpConfig.getPort(),
                    httpConfig.getContextPath());
                // 使用独立的HttpEventController作为Spring的Bean
                new SpringApplicationBuilder(HttpEventController.class).properties("server.port=" + httpConfig.getPort(),
                    "server.servlet.context-path=" + httpConfig.getContextPath()).run();
            } catch (Exception e) {
                log.error("启动HTTP服务器失败", e);
            }
        }).start();
    }
}
