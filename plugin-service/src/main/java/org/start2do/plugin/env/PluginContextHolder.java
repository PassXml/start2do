package org.start2do.plugin.env;

/**
 * 插件运行上下文（线程级隔离）。
 * <p>
 * 设计目标：
 * - 让同一个宿主 Spring Environment 在“插件执行期间”能够读取到该插件自己的配置；
 * - 不把插件配置直接注入到全局 Environment，避免插件之间互相读到彼此的配置。
 */
public final class PluginContextHolder {

    private PluginContextHolder() {
    }

    private static final ThreadLocal<String> PLUGIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<ClassLoader> PLUGIN_CLASS_LOADER = new ThreadLocal<>();

    public static void set(String pluginId, ClassLoader pluginClassLoader) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            clear();
            return;
        }
        PLUGIN_ID.set(pluginId);
        if (pluginClassLoader != null) {
            PLUGIN_CLASS_LOADER.set(pluginClassLoader);
        } else {
            PLUGIN_CLASS_LOADER.remove();
        }
    }

    public static String getPluginId() {
        return PLUGIN_ID.get();
    }

    public static ClassLoader getPluginClassLoader() {
        return PLUGIN_CLASS_LOADER.get();
    }

    public static void clear() {
        PLUGIN_ID.remove();
        PLUGIN_CLASS_LOADER.remove();
    }
}

