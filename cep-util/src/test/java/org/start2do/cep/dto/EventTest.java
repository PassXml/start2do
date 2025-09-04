package org.start2do.cep.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void testEventCreation() {
        Event event = new Event();
        assertNotNull(event);
    }

    @Test
    void testEventWithParameters() {
        String eventId = "test-event-001";
        String eventType = "login_failure";
        String source = "auth_service";
        Map<String, Object> payload = Map.of("userId", "user123", "status", "failed");
        LocalDateTime timestamp = LocalDateTime.now();

        Event event = new Event(eventId, eventType, source, payload, timestamp);

        assertEquals(eventId, event.getEventId());
        assertEquals(eventType, event.getEventType());
        assertEquals(source, event.getSource());
        assertEquals(payload, event.getPayload());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    void testEventBuilderPattern() {
        Event event = new Event()
                .setEventId("test-event-002")
                .setEventType("success_login")
                .setSource("auth_service")
                .setPayload(Map.of("userId", "user456", "status", "success"))
                .setTimestamp(LocalDateTime.now());

        assertEquals("test-event-002", event.getEventId());
        assertEquals("success_login", event.getEventType());
        assertEquals("auth_service", event.getSource());
        assertNotNull(event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testEventToString() {
        Event event = new Event()
                .setEventId("test-event-003")
                .setEventType("test_event")
                .setSource("test_source")
                .setPayload(Map.of("key", "value"))
                .setTimestamp(LocalDateTime.now());

        String toString = event.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test-event-003"));
        assertTrue(toString.contains("test_event"));
        assertTrue(toString.contains("test_source"));
    }
}