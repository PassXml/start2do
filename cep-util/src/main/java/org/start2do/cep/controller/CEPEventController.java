package org.start2do.cep.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.start2do.cep.dto.Event;
import org.start2do.cep.source.HttpEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cep")
@ConditionalOnProperty(name = "flink.http.enabled", havingValue = "true")
public class CEPEventController {
    private static final Logger log = LoggerFactory.getLogger(CEPEventController.class);

    @PostMapping("/events")
    public ResponseEntity<String> inputEvent(@RequestBody EventInputRequest request) {
        try {
            Event event = new Event()
                    .setEventId(request.getEventId())
                    .setEventType(request.getEventType())
                    .setSource(request.getSource())
                    .setPayload(request.getPayload())
                    .setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());

            boolean success = HttpEventSource.putEvent(event);
            if (success) {
                log.info("HTTP event input success: {}", event);
                return ResponseEntity.ok("Event processed successfully");
            } else {
                log.warn("HTTP event input failed (queue full): {}", event);
                return ResponseEntity.status(503).body("Event queue is full");
            }
        } catch (Exception e) {
            log.error("HTTP event input error", e);
            return ResponseEntity.status(500).body("Event processing failed: " + e.getMessage());
        }
    }

    @PostMapping("/events/batch")
    public ResponseEntity<String> inputEventsBatch(@RequestBody List<EventInputRequest> requests) {
        try {
            int successCount = 0;
            for (EventInputRequest request : requests) {
                Event event = new Event()
                        .setEventId(request.getEventId())
                        .setEventType(request.getEventType())
                        .setSource(request.getSource())
                        .setPayload(request.getPayload())
                        .setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());

                if (HttpEventSource.putEvent(event)) {
                    successCount++;
                }
            }

            log.info("HTTP batch event input: {} events processed, {} succeeded", requests.size(), successCount);
            return ResponseEntity.ok(String.format("Batch processed: %d/%d events succeeded", successCount, requests.size()));
        } catch (Exception e) {
            log.error("HTTP batch event input error", e);
            return ResponseEntity.status(500).body("Batch processing failed: " + e.getMessage());
        }
    }

    @GetMapping("/queue/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus() {
        QueueStatusResponse response = new QueueStatusResponse(
                HttpEventSource.getQueueSize(),
                HttpEventSource.getRemainingCapacity(),
                HttpEventSource.isQueueEmpty()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/queue/clear")
    public ResponseEntity<String> clearQueue() {
        HttpEventSource.clearQueue();
        log.info("HTTP event queue cleared");
        return ResponseEntity.ok("Queue cleared successfully");
    }

    public static class EventInputRequest {
        private String eventId;
        private String eventType;
        private String source;
        private Map<String, Object> payload;
        private LocalDateTime timestamp;

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

        public void setPayload(Map<String, Object> payload) {
            this.payload = payload;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class QueueStatusResponse {
        private int currentSize;
        private int remainingCapacity;
        private boolean isEmpty;

        public QueueStatusResponse(int currentSize, int remainingCapacity, boolean isEmpty) {
            this.currentSize = currentSize;
            this.remainingCapacity = remainingCapacity;
            this.isEmpty = isEmpty;
        }

        public int getCurrentSize() {
            return currentSize;
        }

        public void setCurrentSize(int currentSize) {
            this.currentSize = currentSize;
        }

        public int getRemainingCapacity() {
            return remainingCapacity;
        }

        public void setRemainingCapacity(int remainingCapacity) {
            this.remainingCapacity = remainingCapacity;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public void setEmpty(boolean empty) {
            isEmpty = empty;
        }
    }
}