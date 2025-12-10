package org.start2do.plugin.api.event;

import org.start2do.plugin.api.dto.IDomainEventId;

/**
 * 领域事件总线接口
 * <p>
 * 业务侧只需要依赖该接口进行事件发布，具体实现可以是： - 同步调用（进程内） - 持久化后异步分发（推荐）
 */
public interface IDomainEventBus {

    void registerHandle(IDomainEventHandler<?> handler);

    /**
     * 发布领域事件
     *
     * @param event 事件实体
     */
    void publish(IDomainEventId event);

    void unRegisterHandle(IDomainEventHandler<?> eipUtils);

}

