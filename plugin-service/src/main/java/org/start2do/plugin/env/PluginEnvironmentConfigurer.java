package org.start2do.plugin.env;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

/**
 * 将插件隔离属性源挂到宿主 Environment（低优先级），不直接注入任何插件数据。
 */
@Component
@RequiredArgsConstructor
public class PluginEnvironmentConfigurer {

    private final ConfigurableEnvironment environment;
    private final PluginConfigRegistry pluginConfigRegistry;

    @PostConstruct
    public void init() {
        MutablePropertySources sources = environment.getPropertySources();
        if (!sources.contains(PluginIsolatedPropertySource.NAME)) {
            sources.addLast(new PluginIsolatedPropertySource(pluginConfigRegistry));
        }
    }
}
