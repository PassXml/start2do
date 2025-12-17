package org.start2do.plugin.handle;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.start2do.plugin.api.pf4j.Pf4jBridge;
import org.start2do.plugin.env.PluginConfigRegistry;
import org.start2do.plugin.env.PluginContextHolder;

/**
 * PF4J 桥接监听器编排器（推荐方案）
 * <p>
 * 设计目标：
 * 1. 宿主只向 PluginManager 注册一个监听器（本类），桥接模块不再自行 addPluginStateListener
 * 2. 插件 STARTED：按桥接 order 升序分发（例如 DataSource → Mapper → MVC → SOAP）
 * 3. 插件 STOPPED/UNLOADED/DISABLED/FAILED：按桥接 order 降序分发（反向清理，避免资源/Bean 依赖问题）
 * 4. 模块缺席则桥接 Bean 不存在，自然不会参与编排，无需 @DependsOn 硬依赖
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Pf4jBridgeOrchestrator implements PluginStateListener, SmartInitializingSingleton {

    private final PluginManager pluginManager;
    private final ConfigurableApplicationContext applicationContext;
    private final PluginConfigRegistry pluginConfigRegistry;

    private volatile List<Pf4jBridge> orderedBridges = Collections.emptyList();

    @Override
    public void afterSingletonsInstantiated() {
        if (pluginManager == null) {
            log.info("Pf4jBridgeOrchestrator 初始化: 未找到 PluginManager，跳过桥接编排");
            return;
        }

        Map<String, Pf4jBridge> bridgeBeans = applicationContext.getBeansOfType(Pf4jBridge.class);
        List<Pf4jBridge> sorted = bridgeBeans.values().stream()
            .sorted(Comparator.comparingInt(Pf4jBridge::getOrder))
            .collect(Collectors.toList());
        this.orderedBridges = Collections.unmodifiableList(sorted);

        pluginManager.addPluginStateListener(this);
        log.info("Pf4jBridgeOrchestrator 已注册到 PluginManager，桥接顺序={}",
            sorted.stream()
                .map(b -> b.getClass().getSimpleName() + "(" + b.getOrder() + ")")
                .collect(Collectors.toList()));

        // 兜底：若在本编排器注册之前已经有插件处于 STARTED，做一次补偿性桥接。
        for (PluginWrapper wrapper : pluginManager.getPlugins()) {
            if (wrapper.getPluginState() == PluginState.STARTED) {
                dispatchPluginStarted(wrapper);
            }
        }
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        PluginWrapper plugin = event.getPlugin();
        String pluginId = plugin == null ? null : plugin.getPluginId();
        PluginState state = event.getPluginState();

        if (state == PluginState.STARTED) {
            dispatchPluginStarted(plugin);
            return;
        }

        if (state == PluginState.UNLOADED
            || state == PluginState.STOPPED
            || state == PluginState.DISABLED
            || state == PluginState.FAILED) {
            dispatchPluginStopped(plugin);
            if (state == PluginState.UNLOADED) {
                clearSpringCaches(plugin);
            }
        }
    }

    private void dispatchPluginStarted(PluginWrapper plugin) {
        if (plugin == null) {
            return;
        }
        String pluginId = plugin.getPluginId();
        withPluginClassLoader(plugin, () -> {
            // 先加载插件 application*.yml 快照，确保后续桥接/Bean 实例化可读取插件配置
            try {
                pluginConfigRegistry.loadOrReload(pluginId, plugin.getPluginClassLoader(),
                    applicationContext.getEnvironment().getActiveProfiles());
            } catch (Exception ex) {
                // 配置加载失败不应阻断插件启动（插件可能不提供配置文件）
                log.warn("插件 {} 加载 application*.yml 失败(忽略继续)", pluginId, ex);
            }
            for (Pf4jBridge bridge : orderedBridges) {
                try {
                    bridge.onPluginStarted(pluginId);
                } catch (Exception ex) {
                    log.error("桥接执行失败(STARTED): pluginId={}, bridge={}", pluginId, bridge.getClass().getName(), ex);
                    // 注意：是否 unload 由具体 bridge 决定（例如 MVC 桥接内部会 unload），这里不做统一强制策略
                    break;
                }
            }
        });
    }

    private void dispatchPluginStopped(PluginWrapper plugin) {
        if (plugin == null) {
            return;
        }
        String pluginId = plugin.getPluginId();
        withPluginClassLoader(plugin, () -> {
            for (int i = orderedBridges.size() - 1; i >= 0; i--) {
                Pf4jBridge bridge = orderedBridges.get(i);
                try {
                    bridge.onPluginStopped(pluginId);
                } catch (Exception ex) {
                    log.warn("桥接清理失败(忽略继续): pluginId={}, bridge={}", pluginId, bridge.getClass().getName(), ex);
                }
            }
        });
        // 清理插件配置快照，降低类加载器泄漏概率
        try {
            pluginConfigRegistry.unload(pluginId);
        } catch (Exception ex) {
            log.warn("插件 {} 卸载配置失败(忽略继续)", pluginId, ex);
        }
    }

    private void withPluginClassLoader(PluginWrapper plugin, Runnable action) {
        if (plugin == null || action == null) {
            return;
        }
        ClassLoader pluginCl = plugin.getPluginClassLoader();
        Thread t = Thread.currentThread();
        ClassLoader old = t.getContextClassLoader();
        String oldPluginId = PluginContextHolder.getPluginId();
        ClassLoader oldPluginCl = PluginContextHolder.getPluginClassLoader();
        try {
            if (pluginCl != null) {
                t.setContextClassLoader(pluginCl);
            }
            PluginContextHolder.set(plugin.getPluginId(), pluginCl);
            action.run();
        } finally {
            t.setContextClassLoader(old);
            if (oldPluginId != null) {
                PluginContextHolder.set(oldPluginId, oldPluginCl);
            } else {
                PluginContextHolder.clear();
            }
        }
    }

    /**
     * 插件卸载后的缓存清理（降低类加载器泄漏概率）。
     * <p>
     * 说明：Spring 会缓存 JavaBeans Introspector 结果（CachedIntrospectionResults），
     * 若不清理可能导致插件 ClassLoader 被静态缓存间接引用。
     */
    private void clearSpringCaches(PluginWrapper plugin) {
        if (plugin == null) {
            return;
        }
        ClassLoader pluginClassLoader = plugin.getPluginClassLoader();
        if (pluginClassLoader == null) {
            return;
        }
        try {
            CachedIntrospectionResults.clearClassLoader(pluginClassLoader);
            log.info("已清理 Spring CachedIntrospectionResults: pluginId={}", plugin.getPluginId());
        } catch (Exception ex) {
            log.warn("清理 Spring 缓存失败(忽略继续): pluginId={}", plugin.getPluginId(), ex);
        }
    }
}
