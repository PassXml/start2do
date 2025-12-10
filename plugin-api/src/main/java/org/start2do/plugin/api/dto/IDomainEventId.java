package org.start2do.plugin.api.dto;

/**
 * 统一领域事件接口
 * <p>
 * 所有事件必须具备全局唯一 ID 和事件类型两个核心属性，
 * 便于后续做幂等控制与事件路由。
 */
public interface IDomainEventId {

    /**
     * 事件全局唯一 ID，用于幂等控制
     */
    String getId();

    /**
     * 事件类型，用于路由到不同处理器
     */
    String getType();
}

