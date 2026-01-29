package org.start2do.plugin.api.env;

import java.util.Map;

/**
 * 插件配置访问器（由宿主实现并注入到 {@link PluginConfigAccess}）。
 * <p>
 * 设计目标：
 * - 允许插件在非 Spring Bean 场景（例如 PF4J {@code Plugin#start()}）读取配置；
 * - 访问范围以“插件包内 application*.yml 快照”为主，避免直接暴露宿主实现细节；
 * - 宿主实现可自行决定“宿主配置覆盖/合并”策略。
 */
public interface PluginConfigAccessor {

    /**
     * 读取“插件包内”配置快照（不包含宿主配置覆盖）。
     *
     * @param pluginId 插件 ID（PF4J plugin.id）
     * @param key      配置键（例如 demo.greeting）
     * @return 值（可能为 String / Number / List / Map 等），不存在返回 null
     */
    Object getPluginOnly(String pluginId, String key);

    /**
     * 获取“插件包内”配置快照（不包含宿主配置覆盖）。
     *
     * @param pluginId 插件 ID
     * @return 快照（不可变 Map）；若插件未加载配置，返回空 Map
     */
    Map<String, Object> snapshotPluginOnly(String pluginId);

    /**
     * 读取“宿主配置优先”的合并视图（可选能力）。
     * <p>
     * 宿主侧推荐策略：若宿主 Environment 存在该 key，则优先返回宿主值；否则回退到插件快照。
     *
     * @param pluginId 插件 ID
     * @param key      配置键
     * @return 值或 null
     */
    Object getMergedPreferHost(String pluginId, String key);
}

