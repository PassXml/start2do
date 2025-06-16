package org.start2do.cep.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.cep.dto.Event;
import org.start2do.dto.R;

@Slf4j
@RestController
@RequestMapping("/events")
public class HttpEventController {

    // HTTP接口：接收单个事件
    @PostMapping("/single")
    public R receiveEvent(@RequestBody Event event) {
        try {
            HttpEventSource.offerEvent(event);
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
                HttpEventSource.offerEvent(event);
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
        return R.ok(HttpEventSource.getQueueSize());
    }
}
