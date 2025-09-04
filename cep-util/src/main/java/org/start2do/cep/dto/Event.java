package org.start2do.cep.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class Event {
    private String eventId;
    private String eventType;
    private String source;
    private Map<String, Object> payload;
    private LocalDateTime timestamp;

    public Event() {}

    public Event(String eventId, String eventType, String source, Map<String, Object> payload, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.source = source;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public Event setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public Event setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Event setSource(String source) {
        this.source = source;
        return this;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Event setPayload(Map<String, Object> payload) {
        this.payload = payload;
        return this;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Event setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", payload=" + payload +
                ", timestamp=" + timestamp +
                '}';
    }
}