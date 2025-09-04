package org.start2do.cep.source;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.start2do.cep.dto.Event;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class HttpEventSourceTest {
    private static final int TEST_QUEUE_CAPACITY = 1000;

    @BeforeEach
    void setUp() {
        HttpEventSource.clearQueue();
    }

    @AfterEach
    void tearDown() {
        HttpEventSource.clearQueue();
    }

    @Test
    void testPutEventSuccess() {
        Event event = new Event("test-001", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        boolean result = HttpEventSource.putEvent(event);
        assertTrue(result);
    }

    @Test
    void testPutEventWithTimeoutSuccess() throws InterruptedException {
        Event event = new Event("test-002", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        boolean result = HttpEventSource.putEventWithTimeout(event, 5, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    void testQueueStatusMethods() {
        assertTrue(HttpEventSource.isQueueEmpty());
        assertEquals(0, HttpEventSource.getQueueSize());
        
        Event event = new Event("test-003", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        HttpEventSource.putEvent(event);
        
        assertFalse(HttpEventSource.isQueueEmpty());
        assertEquals(1, HttpEventSource.getQueueSize());
        assertTrue(HttpEventSource.getRemainingCapacity() > 0);
    }

    @Test
    void testClearQueue() {
        Event event = new Event("test-004", "test_event", "test_source", 
                Map.of("key", "value"), LocalDateTime.now());
        
        HttpEventSource.putEvent(event);
        assertEquals(1, HttpEventSource.getQueueSize());
        
        HttpEventSource.clearQueue();
        assertEquals(0, HttpEventSource.getQueueSize());
        assertTrue(HttpEventSource.isQueueEmpty());
    }

    @Test
    void testMultipleEvents() {
        for (int i = 0; i < 10; i++) {
            Event event = new Event("test-" + i, "test_event", "test_source", 
                    Map.of("index", i), LocalDateTime.now());
            HttpEventSource.putEvent(event);
        }
        
        assertEquals(10, HttpEventSource.getQueueSize());
        assertFalse(HttpEventSource.isQueueEmpty());
    }

    @Test
    void testSourceCreation() {
        HttpEventSource source = new HttpEventSource();
        assertNotNull(source);
    }
}