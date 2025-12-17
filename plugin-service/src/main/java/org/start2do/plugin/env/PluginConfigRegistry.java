package org.start2do.plugin.env;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 插件配置注册中心：为每个插件维护一份“从插件包 classpath 读取的 application*.yml 配置快照”。
 * <p>
 * 关键约束：
 * - 不把插件配置直接 addFirst/addLast 到宿主 Environment；
 * - 通过 {@link PluginIsolatedPropertySource} 在“插件上下文”内按需解析，保证插件间互相隔离。
 */
@Slf4j
@Component
public class PluginConfigRegistry {

    private static final List<String> BASE_LOCATIONS = Arrays.asList(
        "application.yml",
        "application.yaml",
        "config/application.yml",
        "config/application.yaml"
    );

    private static final List<String> PROFILE_LOCATIONS_PREFIX = Arrays.asList(
        "application-",
        "config/application-"
    );

    private static final List<String> YAML_EXTS = Arrays.asList(".yml", ".yaml");

    private final Map<String, Map<String, Object>> propertiesByPluginId = new ConcurrentHashMap<>();

    /**
     * 读取并覆盖刷新插件配置（建议在插件 STARTED 时调用）。
     */
    public void loadOrReload(String pluginId, ClassLoader pluginClassLoader, String[] activeProfiles) {
        if (pluginId == null || pluginId.trim().isEmpty() || pluginClassLoader == null) {
            return;
        }
        Map<String, Object> merged = loadMergedYaml(pluginId, pluginClassLoader, activeProfiles);
        propertiesByPluginId.put(pluginId, Collections.unmodifiableMap(merged));
        log.info("插件 {} 配置加载完成: keys={}", pluginId, merged.size());
    }

    /**
     * 移除插件配置（建议在插件 STOPPED/UNLOADED 时调用，降低类加载器泄漏概率）。
     */
    public void unload(String pluginId) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return;
        }
        propertiesByPluginId.remove(pluginId);
        log.info("插件 {} 配置已卸载", pluginId);
    }

    public Object getProperty(String pluginId, String key) {
        if (pluginId == null || key == null) {
            return null;
        }
        Map<String, Object> map = propertiesByPluginId.get(pluginId);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    private Map<String, Object> loadMergedYaml(String pluginId, ClassLoader pluginClassLoader, String[] activeProfiles) {
        StandardEnvironment profileEnv = new StandardEnvironment();
        if (activeProfiles != null && activeProfiles.length > 0) {
            profileEnv.setActiveProfiles(activeProfiles);
        }

        Map<String, Object> merged = new LinkedHashMap<>();

        // 1) base: application.yml / config/application.yml
        for (String path : BASE_LOCATIONS) {
            mergeYamlIfExists(merged, pluginId, pluginClassLoader, path, profileEnv);
        }

        // 2) profile: application-{profile}.yml / config/application-{profile}.yml
        if (activeProfiles != null) {
            for (String profile : activeProfiles) {
                if (profile == null || profile.trim().isEmpty()) {
                    continue;
                }
                String p = profile.trim();
                for (String prefix : PROFILE_LOCATIONS_PREFIX) {
                    for (String ext : YAML_EXTS) {
                        String path = prefix + p + ext;
                        mergeYamlIfExists(merged, pluginId, pluginClassLoader, path, profileEnv);
                    }
                }
            }
        }

        return merged;
    }

    private void mergeYamlIfExists(Map<String, Object> merged,
        String pluginId,
        ClassLoader pluginClassLoader,
        String classpathLocation,
        StandardEnvironment profileEnv) {

        Resource res = new ClassPathResource(classpathLocation, pluginClassLoader);
        if (!res.exists()) {
            return;
        }

        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources;
        try {
            sources = loader.load("plugin:" + pluginId + ":" + classpathLocation, res);
        } catch (IOException e) {
            log.warn("插件 {} 读取配置失败(忽略继续): {}", pluginId, classpathLocation, e);
            return;
        }

        for (PropertySource<?> ps : sources) {
            Object raw = ps.getSource();
            if (!(raw instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) raw;

            if (!acceptsActivateOnProfile(map, profileEnv)) {
                continue;
            }

            for (Map.Entry<String, Object> e : map.entrySet()) {
                String key = e.getKey();
                Object value = unwrapOriginValue(e.getValue());
                if (key == null || value == null) {
                    continue;
                }
                merged.put(key, value);
            }
        }

        log.info("插件 {} 已加载配置: {}", pluginId, classpathLocation);
    }

    private boolean acceptsActivateOnProfile(Map<String, Object> map, StandardEnvironment profileEnv) {
        Object v = map.get("spring.config.activate.on-profile");
        if (v == null) {
            // 兼容旧写法（Boot 2.3 及之前）：spring.profiles
            v = map.get("spring.profiles");
        }
        if (v == null) {
            return true;
        }

        String expr = Objects.toString(unwrapOriginValue(v), "").trim();
        if (expr.isEmpty()) {
            return true;
        }

        try {
            return profileEnv.acceptsProfiles(Profiles.of(expr));
        } catch (Exception ex) {
            // 表达式不合法时，为了避免误加载，采用更保守策略：不合格则不合并
            log.warn("检测到无效的 profile 表达式，将跳过该 YAML 文档: expr={}", expr, ex);
            return false;
        }
    }

    private Object unwrapOriginValue(Object value) {
        if (value instanceof OriginTrackedValue) {
            return ((OriginTrackedValue) value).getValue();
        }
        return value;
    }
}
