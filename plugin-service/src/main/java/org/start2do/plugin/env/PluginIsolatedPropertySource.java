package org.start2do.plugin.env;

import org.springframework.core.env.PropertySource;

/**
 * 插件隔离属性源：
 * <p>
 * - 仅当 {@link PluginContextHolder#getPluginId()} 存在时才返回属性；
 * - 属性值来自 {@link PluginConfigRegistry} 中对应插件的配置快照；
 * - 不直接污染宿主 Environment，保证插件间配置隔离。
 */
public class PluginIsolatedPropertySource extends PropertySource<PluginConfigRegistry> {

    public static final String NAME = "pluginIsolated";

    public PluginIsolatedPropertySource(PluginConfigRegistry source) {
        super(NAME, source);
    }

    @Override
    public Object getProperty(String name) {
        String pluginId = PluginContextHolder.getPluginId();
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return null;
        }
        return this.source.getProperty(pluginId, name);
    }
}

