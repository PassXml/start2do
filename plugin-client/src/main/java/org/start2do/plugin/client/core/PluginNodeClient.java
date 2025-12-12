package org.start2do.plugin.client.core;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;
import org.start2do.plugin.client.config.PluginClientProperties;
import org.start2do.plugin.dto.PluginInfo;
import org.start2do.plugin.api.dto.ApiResponse;
import org.start2do.plugin.api.dto.GrayStrategy;
import org.start2do.plugin.api.dto.PluginReleaseConfig;
import org.start2do.plugin.api.dto.PluginSnapshot;
import org.start2do.plugin.service.PluginFileService;

/**
 * 插件节点客户端
 * <p>
 * 主要职责：
 * 1. 定期向 plugin-server 注册 / 上报心跳
 * 2. 定期拉取插件发布快照
 * 3. 根据快照与本地插件情况比对，决定是否拉取 / 更新插件（当前只预留扩展点）
 */
@Slf4j
public class PluginNodeClient {

    private final PluginClientProperties properties;
    private final RestTemplate restTemplate;
    private final PluginFileService pluginFileService;
    private final TaskScheduler scheduler;

    /**
     * 记录各插件最近一次同步失败的错误信息
     * key: pluginId, value: error message
     */
    private final Map<String, String> lastSyncErrors = new HashMap<String, String>();

    public PluginNodeClient(PluginClientProperties properties,
        RestTemplate restTemplate,
        PluginFileService pluginFileService) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.pluginFileService = pluginFileService;
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("plugin-node-client-");
        taskScheduler.initialize();
        this.scheduler = taskScheduler;
    }

    @PostConstruct
    public void start() {
        log.info("插件客户端初始化完成, server={}",
            properties.getServerBaseUrl());
        long interval = properties.getHeartbeatIntervalSeconds();
        scheduler.scheduleAtFixedRate(this::heartbeatAndSync, interval * 1000);
    }

    /**
     * 心跳 + 同步插件版本
     * <p>
     * 默认仅由 {@link #start()} 中的定时任务调用。
     * 为了便于在测试环境或自定义场景中主动触发一次心跳，
     * 这里使用 protected 访问级别，允许子类覆盖或显式调用。
     */
    protected void heartbeatAndSync() {
        try {
            Map<String, String> localPlugins = buildLocalPluginVersionView();
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("nodeId", resolveNodeId());
            body.put("appName", properties.getAppName());
            body.put("ip", resolveLocalIp());
            body.put("port", null);
            body.put("tags", new HashMap<String, String>());
            body.put("plugins", localPlugins);
            body.put("pluginErrors", new HashMap<String, String>(lastSyncErrors));
            restTemplate.postForObject(
                properties.getServerBaseUrl() + "/api/nodes/register",
                body,
                Map.class
            );
            ResponseEntity<ApiResponse<PluginSnapshot>> resp = restTemplate.exchange(
                properties.getServerBaseUrl() + "/api/plugins/snapshot",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<PluginSnapshot>>() {
                }
            );
            PluginSnapshot snapshot = resp != null && resp.getBody() != null ? resp.getBody().getData() : null;
            if (snapshot != null) {
                applySnapshot(snapshot, localPlugins);
            }
        } catch (Exception e) {
            log.warn("插件客户端心跳或同步失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建本地插件版本视图
     * <p>
     * 约定插件文件命名为：pluginId-version.jar
     * 这里从文件名中解析出版本号，仅统计已加载到 PF4J 的插件（PluginInfo.pluginId != null）。
     */
    private Map<String, String> buildLocalPluginVersionView() {
        Map<String, String> result = new HashMap<String, String>();
        List<PluginInfo> list = pluginFileService.listPlugins();
        if (list == null) {
            return result;
        }
        for (PluginInfo info : list) {
            if (info.getPluginId() != null) {
                String version = resolveVersionFromFileName(info.getFileName());
                result.put(info.getPluginId(), version);
            }
        }
        return result;
    }

    /**
     * 从约定的文件名中解析版本号：pluginId-version.jar
     * <p>
     * 如果解析失败，则回退使用完整文件名，避免信息丢失。
     */
    private String resolveVersionFromFileName(String fileName) {
        if (fileName == null || !fileName.endsWith(".jar")) {
            return fileName;
        }
        String nameWithoutExt = fileName.substring(0, fileName.length() - ".jar".length());
        int idx = nameWithoutExt.lastIndexOf("-");
        if (idx <= 0 || idx == nameWithoutExt.length() - 1) {
            // 不符合约定格式，直接返回原始文件名以便排查
            return fileName;
        }
        return nameWithoutExt.substring(idx + 1);
    }

    /**
     * 根据管理端快照与本地插件视图决定是否拉取 / 更新插件
     * <p>
     * 当前实现：
     * - 根据发布配置中的 stableVersion / grayVersion / grayStrategy，
     *   为当前节点选择目标版本（全部下发或部分节点下发）；
     * - 仅负责“分发哪一个版本到本节点”，具体灰度逻辑由插件内部实现。
     */
    private void applySnapshot(PluginSnapshot snapshot, Map<String, String> local) {
        if (snapshot.getReleases() == null) {
            return;
        }
        String nodeId = resolveNodeId();
        for (PluginReleaseConfig cfg : snapshot.getReleases()) {
            String pluginId = cfg.getPluginId();
            String targetVersion = selectVersionForNode(cfg, nodeId);
            String localVersion = local.get(pluginId);
            log.debug("插件快照对比, pluginId={}, target={}, local={}", pluginId, targetVersion, localVersion);

            // 无目标版本时视为“移除插件”指令：若本地仍存在对应插件，则尝试删除
            if (targetVersion == null || targetVersion.trim().isEmpty()) {
                if (localVersion != null) {
                    try {
                        pluginFileService.deleteByPluginId(pluginId);
                        // 清除错误记录
                        lastSyncErrors.remove(pluginId);
                        log.info("根据管理端指令移除本地插件, pluginId={}", pluginId);
                    } catch (Exception ex) {
                        log.warn("根据管理端指令移除插件 {} 失败: {}", pluginId, ex.getMessage(), ex);
                        lastSyncErrors.put(pluginId, ex.getMessage());
                    }
                }
                continue;
            }

            if (localVersion == null || !targetVersion.equals(localVersion)) {
                try {
                    downloadAndActivate(pluginId, targetVersion);
                    // 同步成功后清除错误记录
                    lastSyncErrors.remove(pluginId);
                } catch (Exception ex) {
                    log.warn("插件 {} 同步到版本 {} 失败: {}", pluginId, targetVersion, ex.getMessage(), ex);
                    lastSyncErrors.put(pluginId, ex.getMessage());
                }
            }
        }
    }

    /**
     * 根据发布配置与当前节点信息，选择本节点需要分发的插件版本。
     * <p>
     * 规则：
     * - 若 grayVersion 为空，则全量下发 stableVersion；
     * - 若 grayStrategy 为空或类型不识别，默认仍下发 stableVersion；
     * - 若 grayStrategy.type = NODE，则仅在 nodeIds 列表中的节点下发 grayVersion，其余节点仍下发 stableVersion。
     * <p>
     * 注意：具体“如何灰度”（例如请求级别选择 stable/gray）由插件内部实现，
     * 这里仅控制“是否将该版本分发到本节点”。
     */
    private String selectVersionForNode(PluginReleaseConfig cfg, String nodeId) {
        String stable = cfg.getStableVersion();
        String gray = cfg.getGrayVersion();
        GrayStrategy strategy = cfg.getGrayStrategy();

        // 无灰度版本时，全量下发 stable
        if (gray == null || gray.trim().isEmpty()) {
            return stable;
        }

        // 无策略或未指定类型时，保守地只分发 stable
        if (strategy == null || strategy.getType() == null) {
            return stable;
        }

        String type = strategy.getType().toUpperCase();

        // 显式指定节点灰度：仅 nodeIds 包含当前 nodeId 的节点下发 gray 版本
        if ("NODE".equals(type) && strategy.getNodeIds() != null && !strategy.getNodeIds().isEmpty()) {
            if (strategy.getNodeIds().contains(nodeId)) {
                return gray;
            }
            return stable;
        }

        // 其它类型（未识别）统一回退到 stable，避免隐式自动分流
        return stable;
    }

    /**
     * 从管理端下载指定插件版本，并通过 PluginFileService 写入本地并启用
     */
    private void downloadAndActivate(String pluginId, String version) throws Exception {
        String url = properties.getServerBaseUrl()
            + "/api/plugins/download?pluginId=" + pluginId + "&version=" + version;
        log.info("开始从管理端同步插件: pluginId={}, version={}, url={}", pluginId, version, url);

        ResponseEntity<byte[]> resp = restTemplate.getForEntity(url, byte[].class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("下载插件失败, httpStatus=" + resp.getStatusCode());
        }

        byte[] data = resp.getBody();
        File tmp = File.createTempFile("plugin-sync-" + pluginId + "-" + version + "-", ".jar");
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(data);
            fos.flush();
        }

        try {
            // 交由 PluginFileService 统一处理命名规则与 PF4J 启动
            pluginFileService.upload(tmp, true, pluginId + "-" + version + ".jar");
            log.info("插件已同步并启用成功: pluginId={}, version={}", pluginId, version);
        } finally {
            // 不保留临时文件
            if (tmp.exists()) {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }
        }
    }

    private String resolveNodeId() {
        if (properties.getNodeId() != null && properties.getNodeId().trim().length() > 0) {
            return properties.getNodeId();
        }
        return properties.getAppName() + "@" + resolveLocalIp();
    }

    private String resolveLocalIp() {
        // 优先使用配置中的 IP（适用于 Docker / K8s 等容器环境需要显式指定对外 IP 的场景）
        if (properties.getIp() != null && properties.getIp().trim().length() > 0) {
            return properties.getIp().trim();
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}
