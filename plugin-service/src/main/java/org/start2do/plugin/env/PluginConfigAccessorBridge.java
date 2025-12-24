package org.start2do.plugin.env;

import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.start2do.plugin.api.env.PluginConfigAccess;
import org.start2do.plugin.api.env.PluginConfigAccessor;

/**
 * 宿主侧插件配置访问器桥接：
 * <p>
 * - 将宿主的 {@link PluginConfigRegistry} 能力暴露给插件侧 {@link PluginConfigAccess}；
 * - 允许插件在 PF4J Plugin#start 等“非 Spring Bean 场景”读取配置快照。
 */
@Component
@RequiredArgsConstructor
public class PluginConfigAccessorBridge implements PluginConfigAccessor {

    private final PluginConfigRegistry pluginConfigRegistry;
    private final Environment environment;

    @PostConstruct
    public void init() {
        PluginConfigAccess.setAccessor(this);
    }

    @Override
    public Object getPluginOnly(String pluginId, String key) {
        return pluginConfigRegistry.getProperty(pluginId, key);
    }

    @Override
    public Map<String, Object> snapshotPluginOnly(String pluginId) {
        return pluginConfigRegistry.snapshot(pluginId);
    }

    @Override
    public Object getMergedPreferHost(String pluginId, String key) {
        if (key == null) {
            return null;
        }
        String host = environment.getProperty(key);
        if (host != null) {
            return host;
        }
        return pluginConfigRegistry.getProperty(pluginId, key);
    }
}

