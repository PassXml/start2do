package org.start2do.plugin.handle;

import java.io.File;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.env.Environment;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.start2do.plugin.config.PluginSystemProperties;
import org.start2do.plugin.env.PluginConfigRegistry;
import org.start2do.plugin.service.PluginRuntimeRegistry;

/**
 * 插件系统启动初始化组件
 * <p>
 * 负责在应用启动时： 1. 确认并创建插件存储目录 2. 检查 PF4J 是否启用（eip.pf4j.enabled=true） 3. 打印当前已加载的插件信息，方便排查问题
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginSystemInitializer {


    private final PluginSystemProperties pluginSystemProperties;
    private final PluginConfigRegistry pluginConfigRegistry;
    private final Environment environment;
    private final PluginRuntimeRegistry pluginRuntimeRegistry;

    /**
     * PF4J 插件管理器，由 plugin-bridge 自动配置。
     * 当 eip.pf4j.enabled != true 时，该 Bean 不存在。
     */
    private final PluginManager pluginManager;

    /**
     * 监听 SpringBoot ApplicationReadyEvent，确保 Spring 完成初始化后再加载插件系统
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String path = pluginSystemProperties.getRuntime().getStoragePath();
        if (path == null || path.trim().isEmpty()) {
            log.warn("插件系统初始化: 未配置 plugin.storage-path，PF4J 将无法加载任何插件");
        } else {
            File dir = new File(path);
            if (!dir.exists() && !dir.mkdirs()) {
                log.warn("插件系统初始化: 无法创建插件目录: {}", dir.getAbsolutePath());
            } else {
                log.info("插件系统初始化: 使用插件目录: {}", dir.getAbsolutePath());
            }
        }
        if (pluginManager == null) {
            log.info("插件系统初始化: PF4J 未启用，当前仅支持插件文件管理");
            return;
        }
        pluginManager.loadPlugins();
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            try {
                // 预加载插件包内 application*.yml，便于插件在 Plugin#start 等阶段读取配置
                try {
                    pluginConfigRegistry.loadOrReload(plugin.getPluginId(), plugin.getPluginClassLoader(),
                        environment.getActiveProfiles());
                } catch (Exception ex) {
                    // 配置不存在或解析失败不应阻断插件启动
                    log.warn("插件 {} 预加载 application*.yml 失败(忽略继续)", plugin.getPluginId(), ex);
                }
                pluginManager.startPlugin(plugin.getDescriptor().getPluginId());
                log.info("插件状态：{},{}", plugin.getDescriptor().getPluginId(), plugin.getPluginState());
                try {
                    if (plugin.getPluginPath() != null) {
                        pluginRuntimeRegistry.recordJarFingerprint(plugin.getPluginId(), plugin.getPluginPath());
                    }
                } catch (Exception ignore) {
                    // 忽略记录失败
                }
            } catch (Exception e) {
                pluginManager.unloadPlugin(plugin.getDescriptor().getPluginId());
                try {
                    pluginConfigRegistry.unload(plugin.getPluginId());
                } catch (Exception ignore) {
                    // 忽略卸载配置失败
                }
                try {
                    pluginRuntimeRegistry.clear(plugin.getPluginId());
                } catch (Exception ignore) {
                    // 忽略清理失败
                }
                log.error("插件启动失败：{},{},{}", plugin.getPluginId(), plugin.getPluginPath(), e.getMessage());
            }
        }
        // 此时 PluginManager 已由 EipPf4jBridgeConfig.loadPlugins/startPlugins 初始化
        String pluginIds = pluginManager.getPlugins().stream()
            .map(p -> p.getDescriptor().getPluginId())
            .collect(Collectors.joining(", "));
        log.info("插件系统初始化: PF4J 已启用，已加载插件: {}", pluginIds.isEmpty() ? "<无>" : pluginIds);
    }
}
