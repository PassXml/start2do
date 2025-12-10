package org.start2do.plugin.api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 插件事件持久化实体
 * <p>
 * 用于将领域事件持久化到数据库，支持崩溃恢复与重试。
 */
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class PluginEvent implements Serializable {

    /**
     * 事件 ID，对应 DomainEvent.getId()
     */
    private String id;

    /**
     * 事件类型，对应 DomainEvent.getType()
     */
    private String eventType;

    /**
     * 序列化后的事件内容（JSON）
     */
    private String payload;

    /**
     * 事件状态：PENDING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 下次允许重试的时间
     */
    private LocalDateTime nextRetryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    /**
     * 标记事件处理成功
     */
    public void markSuccess() {
        this.status = STATUS_SUCCESS;
        this.nextRetryTime = null;
    }

    /**
     * 标记事件处理失败并计算下次重试时间
     * <p>
     * 简单实现：每次失败延迟 retryCount 分钟，可按需要调整为指数退避。
     */
    public void markFailedAndScheduleRetry() {
        this.status = STATUS_FAILED;
        if (retryCount == null) {
            retryCount = 0;
        }
        retryCount++;
        this.nextRetryTime = LocalDateTime.now().plusMinutes(retryCount);
    }
}

