package org.start2do.plugin.api.event;

import org.start2do.plugin.api.dto.IDomainEventId;

/**
 * 插件事件处理扩展点
 * <p>
 * 插件通过实现该接口来订阅特定类型的事件。
 */
public interface IDomainEventHandler<T extends IDomainEventId> {

    /**
     * 声明该处理器支持的事件类型，
     * 通常与事件类的 {@link IDomainEventId#getType()} 返回值一致。
     */
    String supportsType();

    /**
     * 处理事件
     *
     * @param event 事件实体（调用方需确保类型匹配）
     * @throws Exception 处理过程中出现的异常
     */
    void onEvent(T event) throws Exception;
}

