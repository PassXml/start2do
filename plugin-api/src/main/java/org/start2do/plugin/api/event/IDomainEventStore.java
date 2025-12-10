package org.start2do.plugin.api.event;

import org.start2do.plugin.api.dto.PluginEvent;

/**
 * 领域事件总线接口
 * <p>
 * 业务侧只需要依赖该接口进行事件发布，具体实现可以是： - 同步调用（进程内） - 持久化后异步分发（推荐）
 */
public interface IDomainEventStore {

    default void store(PluginEvent event) {

    }

    /**
     * 根据事件 ID 触发重试
     * <p>
     * 实现方可以根据 eventId 从持久化介质中加载事件，并重新分发给订阅者； 也可以根据自身业务需要决定是否支持重试。 默认实现为空，不做任何处理。
     *
     * @param eventId 事件唯一标识
     */
    default void retry(String eventId) {
        // 默认不实现，由下游按需覆盖
    }

    default void complate(PluginEvent event) {

    }
}

