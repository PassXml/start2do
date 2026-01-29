package org.start2do.plugin.env;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * 插件隔离属性源：
 * <p>
 * - 仅当 {@link PluginContextHolder#getPluginId()} 存在时才返回属性；
 * - 属性值来自 {@link PluginConfigRegistry} 中对应插件的配置快照；
 * - 不直接污染宿主 Environment，保证插件间配置隔离。
 */
public class PluginIsolatedPropertySource extends EnumerablePropertySource<PluginConfigRegistry> {

    public static final String NAME = "pluginIsolated";
    private static final String[] EMPTY_NAMES = new String[0];

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

    @Override
    public String[] getPropertyNames() {
        String pluginId = PluginContextHolder.getPluginId();
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return EMPTY_NAMES;
        }
        Map<String, Object> snapshot = this.source.snapshot(pluginId);
        if (snapshot == null || snapshot.isEmpty()) {
            return EMPTY_NAMES;
        }
        List<String> names = new ArrayList<>(snapshot.size());
        for (String k : snapshot.keySet()) {
            if (k != null && !k.trim().isEmpty()) {
                names.add(k);
            }
        }
        return names.isEmpty() ? EMPTY_NAMES : names.toArray(new String[0]);
    }
}

