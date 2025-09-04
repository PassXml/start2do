package org.start2do.cep.source;

import org.apache.flink.streaming.api.functions.source.legacy.SourceFunction;
import org.start2do.cep.dto.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueEventSource implements SourceFunction<Event> {
    private static final Logger log = LoggerFactory.getLogger(QueueEventSource.class);
    private volatile boolean isRunning = true;
    private final BlockingQueue<Event> eventQueue;
    private static final int DEFAULT_QUEUE_CAPACITY = 10000;
    private static final BlockingQueue<Event> staticQueue = new LinkedBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);

    public QueueEventSource() {
        this.eventQueue = staticQueue;
    }

    public static boolean putEvent(Event event) {
        return staticQueue.offer(event);
    }

    public static boolean putEventWithTimeout(Event event, long timeout, TimeUnit unit) throws InterruptedException {
        return staticQueue.offer(event, timeout, unit);
    }

    public static int getQueueSize() {
        return staticQueue.size();
    }

    public static int getRemainingCapacity() {
        return staticQueue.remainingCapacity();
    }

    public static boolean isQueueEmpty() {
        return staticQueue.isEmpty();
    }

    public static void clearQueue() {
        staticQueue.clear();
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        while (isRunning) {
            try {
                Event event = eventQueue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    ctx.collect(event);
                    log.debug("Emitted event: {}", event);
                }
            } catch (InterruptedException e) {
                if (!isRunning) {
                    break;
                }
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}