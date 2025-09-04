package org.start2do.cep.source;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.start2do.cep.dto.Event;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class QueueEventSourceTest {

    @BeforeEach
    void setUp() {
        QueueEventSource.clearQueue();
    }

    @AfterEach
    void tearDown() {
        QueueEventSource.clearQueue();
    }

    @Test
    void testPutEventSuccess() {
        Event event = new Event("queue-test-001", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        boolean result = QueueEventSource.putEvent(event);
        assertTrue(result);
    }

    @Test
    void testPutEventWithTimeoutSuccess() throws InterruptedException {
        Event event = new Event("queue-test-002", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        boolean result = QueueEventSource.putEventWithTimeout(event, 5, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    void testQueueStatusMethods() {
        assertTrue(QueueEventSource.isQueueEmpty());
        assertEquals(0, QueueEventSource.getQueueSize());
        
        Event event = new Event("queue-test-003", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        QueueEventSource.putEvent(event);
        
        assertFalse(QueueEventSource.isQueueEmpty());
        assertEquals(1, QueueEventSource.getQueueSize());
        assertTrue(QueueEventSource.getRemainingCapacity() > 0);
    }

    @Test
    void testClearQueue() {
        Event event = new Event("queue-test-004", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        QueueEventSource.putEvent(event);
        assertEquals(1, QueueEventSource.getQueueSize());
        
        QueueEventSource.clearQueue();
        assertEquals(0, QueueEventSource.getQueueSize());
        assertTrue(QueueEventSource.isQueueEmpty());
    }

    @Test
    void testMultipleEvents() {
        for (int i = 0; i < 20; i++) {
            Event event = new Event("queue-test-" + i, "test_event", "test_source", 
                    Map.of("index", i), LocalDateTime.now());
            QueueEventSource.putEvent(event);
        }
        
        assertEquals(20, QueueEventSource.getQueueSize());
        assertFalse(QueueEventSource.isQueueEmpty());
    }

    @Test
    void testSourceCreation() {
        QueueEventSource source = new QueueEventSource();
        assertNotNull(source);
    }

    @Test
    void testHighVolumeEvents() {
        int eventCount = 1000;
        for (int i = 0; i < eventCount; i++) {
            Event event = new Event("high-volume-" + i, "stress_test", "test_source", 
                    Map.of("index", i, "batch", "large"), LocalDateTime.now());
            QueueEventSource.putEvent(event);
        }
        
        assertEquals(eventCount, QueueEventSource.getQueueSize());
    }
}