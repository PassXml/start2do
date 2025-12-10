package org.start2do.plugin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.start2do.plugin.api.dto.IDomainEventId;
import org.start2do.plugin.api.dto.PluginEvent;
import org.start2do.plugin.api.event.IDomainEventBus;
import org.start2do.plugin.api.event.IDomainEventHandler;
import org.start2do.plugin.api.event.IDomainEventStore;

/**
 * 基于数据库的领域事件总线实现
 * <p>
 * 特性：
 * <ul>
 *     <li>发布事件时先持久化到 plugin_event 表，保证事件不丢失</li>
 *     <li>持久化成功后，再分发给已注册的 {@link IDomainEventHandler} 处理器</li>
 *     <li>处理结果会回写到 PluginEvent.status / retryCount / nextRetryTime 字段，便于后续重试与监控</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventBus implements IDomainEventBus {

    @Getter
    private static DomainEventBus DOMAIN_EVENT_BUS;
    private final IDomainEventStore domainEventStore;

    private final ObjectMapper objectMapper;

    /**
     * 事件类型 -> 处理器列表
     */
    private final Map<String, List<IDomainEventHandler<?>>> handlerMapping = new ConcurrentHashMap<>();

    @Override
    public void registerHandle(IDomainEventHandler<?> handler) {
        if (handler == null) {
            return;
        }
        String type = handler.supportsType();
        if (type == null || type.isEmpty()) {
            log.warn("注册领域事件处理器失败，supportsType 为空, handler={}", handler.getClass().getName());
            return;
        }
        // 按事件类型维度维护处理器列表，支持同一类型多个处理器
        handlerMapping.compute(type, (key, handlers) -> {
            List<IDomainEventHandler<?>> list = handlers == null ? new CopyOnWriteArrayList<>() : handlers;
            if (!list.contains(handler)) {
                list.add(handler);
            }
            return list;
        });
    }

    @Override
    public void unRegisterHandle(IDomainEventHandler<?> handler) {
        if (handler == null) {
            return;
        }
        String type = handler.supportsType();
        if (type == null || type.isEmpty()) {
            return;
        }
        handlerMapping.computeIfPresent(type, (key, handlers) -> {
            handlers.remove(handler);
            // 若该事件类型已无处理器，则从映射中移除，避免内存泄露
            return handlers.isEmpty() ? null : handlers;
        });
    }

    @PostConstruct
    public void init() {
        DomainEventBus.DOMAIN_EVENT_BUS = this;

    }

    @Override
    public void publish(IDomainEventId event) {
        if (event == null) {
            log.warn("发布领域事件时传入 null，忽略本次调用");
            return;
        }

        // 1. 持久化事件
        PluginEvent record = new PluginEvent();
        try {
            record.setId(event.getId());
            record.setEventType(event.getType());
            record.setPayload(serializeEvent(event));
            record.setStatus(PluginEvent.STATUS_PENDING);
            record.setRetryCount(0);
            domainEventStore.store(record);
        } catch (Exception e) {
            log.error("插件事件持久化失败, type={}, id={}", safeType(event), safeId(event), e);
            throw new RuntimeException("插件事件持久化失败:" + e.getMessage());
        }

        // 2. 持久化成功后分发给插件处理器
        dispatch(record, event);
    }

    /**
     * 将事件序列化为 JSON 字符串
     */
    private String serializeEvent(IDomainEventId event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    /**
     * 将事件路由到对应的插件处理器
     */
    private void dispatch(PluginEvent record, IDomainEventId event) {
        List<IDomainEventHandler<?>> handlers = handlerMapping.getOrDefault(event.getType(), Collections.emptyList());

        if (handlers.isEmpty()) {
            log.debug("当前无插件订阅该事件, type={}", event.getType());
            return;
        }

        boolean success = true;
        for (IDomainEventHandler<?> handler : handlers) {
            try {
                invokeHandler(handler, event);
            } catch (Exception ex) {
                success = false;
                log.error("插件事件处理失败, type={}, handler={}", event.getType(), handler.getClass().getName(), ex);
            }
        }

        // 根据处理结果更新事件状态，便于后续重试与监控
        try {
            if (success) {
                record.markSuccess();
            } else {
                record.markFailedAndScheduleRetry();
            }
            domainEventStore.complate(record);
        } catch (Exception e) {
            log.error("更新插件事件状态失败, type={}, id={}", record.getEventType(), record.getId(), e);
        }
    }

    /**
     * 统一处理泛型擦除带来的强转问题
     */
    @SuppressWarnings("unchecked")
    private <T extends IDomainEventId> void invokeHandler(IDomainEventHandler<T> handler, IDomainEventId event)
        throws Exception {
        handler.onEvent((T) event);
    }

    private String safeType(IDomainEventId event) {
        try {
            return event.getType();
        } catch (Exception e) {
            log.error("未知类型，{},{}", event, e.getMessage(), e);
            return "UNKNOWN";
        }
    }

    private String safeId(IDomainEventId event) {
        try {
            return event.getId();
        } catch (Exception e) {
            log.error("未知类型ID，{},{}", event, e.getMessage(), e);
            return "UNKNOWN";
        }
    }
}
