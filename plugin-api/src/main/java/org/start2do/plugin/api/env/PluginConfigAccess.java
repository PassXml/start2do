package org.start2do.plugin.api.env;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;

/**
 * 插件侧统一的配置读取入口。
 * <p>
 * 用法示例（PF4J Plugin#start 中）：
 * <pre>
 * String pluginId = getWrapper().getPluginId();
 * Object v = PluginConfigAccess.getPluginOnly(pluginId, "demo.greeting");
 * </pre>
 *
 * 说明：
 * - 具体读取能力由宿主通过 {@link #setAccessor(PluginConfigAccessor)} 注入；
 * - 若宿主未启用 plugin-service（或未注入访问器），这些方法会返回 null/空 Map（保守降级）。
 */
public final class PluginConfigAccess {

    private PluginConfigAccess() {
    }

    private static volatile PluginConfigAccessor accessor;

    /**
     * 宿主侧注入配置访问器（通常在 Spring 容器启动完成后执行一次）。
     */
    public static void setAccessor(PluginConfigAccessor accessor) {
        PluginConfigAccess.accessor = accessor;
    }

    /**
     * 读取“插件包内”配置快照（不包含宿主配置覆盖）。
     */
    public static Object getPluginOnly(String pluginId, String key) {
        PluginConfigAccessor a = accessor;
        if (a == null) {
            return null;
        }
        return a.getPluginOnly(pluginId, key);
    }

    /**
     * 读取“插件包内”配置快照（不包含宿主配置覆盖），支持 prefix + key 组合输入。
     * <p>
     * 等价于：{@code getPluginOnly(pluginId, prefix + "." + key)}（会自动处理 "." 拼接细节）。
     *
     * @param pluginId 插件 ID
     * @param prefix   前缀（例如 demo / demo.sub / demo.sub.）
     * @param key      键（例如 greeting / sub.greeting / .greeting）
     * @return 值或 null
     */
    public static Object getPluginOnly(String pluginId, String prefix, String key) {
        String fullKey = joinKey(prefix, key);
        if (fullKey == null) {
            return null;
        }
        return getPluginOnly(pluginId, fullKey);
    }

    /**
     * 获取“插件包内”配置快照（不包含宿主配置覆盖）。
     */
    public static Map<String, Object> snapshotPluginOnly(String pluginId) {
        PluginConfigAccessor a = accessor;
        if (a == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = a.snapshotPluginOnly(pluginId);
        return map == null ? Collections.emptyMap() : map;
    }

    /**
     * 获取“插件包内”配置快照（不包含宿主配置覆盖），按 prefix 过滤并返回“去前缀”的键集合。
     * <p>
     * 例如快照中存在：
     * demo.greeting=hi, demo.timeout=1000
     * 则调用 {@code snapshotPluginOnly(pluginId, "demo")} 返回：
     * greeting=hi, timeout=1000
     *
     * @param pluginId 插件 ID
     * @param prefix   前缀（例如 demo / demo.sub / demo.sub.）
     * @return 去前缀后的快照 Map；若无匹配项返回空 Map
     */
    public static Map<String, Object> snapshotPluginOnly(String pluginId, String prefix) {
        Map<String, Object> all = snapshotPluginOnly(pluginId);
        String normalized = normalizePrefix(prefix);
        if (normalized == null) {
            return all;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : all.entrySet()) {
            String k = e.getKey();
            if (k == null || !k.startsWith(normalized)) {
                continue;
            }
            String stripped = k.substring(normalized.length());
            if (stripped.isEmpty()) {
                continue;
            }
            out.put(stripped, e.getValue());
        }
        return out.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(out);
    }

    /**
     * 读取“宿主配置优先”的合并视图（可选能力）。
     */
    public static Object getMergedPreferHost(String pluginId, String key) {
        PluginConfigAccessor a = accessor;
        if (a == null) {
            return null;
        }
        return a.getMergedPreferHost(pluginId, key);
    }

    /**
     * 读取“宿主配置优先”的合并视图（可选能力），支持 prefix + key 组合输入。
     */
    public static Object getMergedPreferHost(String pluginId, String prefix, String key) {
        String fullKey = joinKey(prefix, key);
        if (fullKey == null) {
            return null;
        }
        return getMergedPreferHost(pluginId, fullKey);
    }

    private static String joinKey(String prefix, String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        String k = key.trim();
        while (k.startsWith(".")) {
            k = k.substring(1);
        }
        if (prefix == null || prefix.trim().isEmpty()) {
            return k;
        }
        String p = prefix.trim();
        while (p.endsWith(".")) {
            p = p.substring(0, p.length() - 1);
        }
        if (p.isEmpty()) {
            return k;
        }
        return p + "." + k;
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return null;
        }
        String p = prefix.trim();
        while (p.endsWith(".")) {
            p = p.substring(0, p.length() - 1);
        }
        if (p.isEmpty()) {
            return null;
        }
        return p + ".";
    }
}
