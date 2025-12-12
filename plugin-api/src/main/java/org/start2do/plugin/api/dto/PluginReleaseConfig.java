package org.start2do.plugin.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 单个插件当前发布配置 + 历史
 */
@Data
public class PluginReleaseConfig {

    private String pluginId;

    /**
     * 当前稳定版本
     */
    private String stableVersion;

    /**
     * 当前灰度版本（可为空）
     */
    private String grayVersion;

    /**
     * 灰度策略
     */
    private GrayStrategy grayStrategy;

    /**
     * 发布历史记录
     */
    private List<ReleaseHistoryItem> history = new ArrayList<ReleaseHistoryItem>();

    @Data
    public static class ReleaseHistoryItem {

        private String fromVersion;
        private String toVersion;

        /**
         * 操作类型：DEPLOY / GRAY / ROLLBACK / REMOVE
         */
        private String operation;

        /**
         * 操作时间
         * <p>
         * 约定格式：yyyy-MM-dd HH:mm:ss
         * 时区：默认使用系统时区（ZoneId.systemDefault()）
         */
        private String operateTime;

        private String operator;
        private String remark;
    }
}
