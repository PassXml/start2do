package org.start2do.plugin.server.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.plugin.api.dto.ApiResponse;
import org.start2do.plugin.api.dto.GrayStrategy;
import org.start2do.plugin.api.dto.PluginReleaseConfig;
import org.start2do.plugin.api.dto.PluginSnapshot;
import org.start2do.plugin.server.model.NodeInfo;
import org.start2do.plugin.server.model.PluginVersionInfo;
import org.start2do.plugin.server.service.NodeRegistryService;
import org.start2do.plugin.server.service.PluginAdminService;
import org.start2do.plugin.server.web.dto.NodePluginStatus;
import org.start2do.plugin.server.web.dto.PluginDeployRequest;
import org.start2do.plugin.server.web.dto.PluginGrayNodeStatus;
import org.start2do.plugin.server.web.dto.PluginGrayNodesRequest;

/**
 * 插件版本管理与发布接口
 * <p>
 * 统一由该模块进行插件上传、版本管理、灰度发布与回滚。
 */
@RestController
@RequestMapping("/api/plugins/server")
@RequiredArgsConstructor
public class PluginAdminController {

    private final PluginAdminService pluginAdminService;
    private final NodeRegistryService nodeRegistryService;

    /**
     * 上传插件新版本
     */
    @PostMapping(value = "/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PluginVersionInfo> upload(
        @RequestParam(value = "changelog", required = false) String changelog,
        @RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.success(pluginAdminService.upload(file, changelog));
    }

    /**
     * 查询插件所有版本
     */
    @GetMapping("/versions")
    public ApiResponse<List<PluginVersionInfo>> listVersions(@RequestParam("pluginId") String pluginId) {
        return ApiResponse.success(pluginAdminService.listVersions(pluginId));
    }

    /**
     * 查询指定插件的灰度节点列表视图（用于管理端“按节点勾选灰度”）
     */
    @GetMapping("/gray/nodes")
    public ApiResponse<List<PluginGrayNodeStatus>> listGrayNodes(@RequestParam("pluginId") String pluginId) {
        PluginReleaseConfig release = pluginAdminService.getRelease(pluginId);
        String stableVersion = release != null ? release.getStableVersion() : null;
        String grayVersion = release != null ? release.getGrayVersion() : null;

        Set<String> grayNodeIds = new HashSet<>();
        if (release != null) {
            GrayStrategy strategy = release.getGrayStrategy();
            if (strategy != null
                && "NODE".equalsIgnoreCase(strategy.getType())
                && strategy.getNodeIds() != null) {
                grayNodeIds.addAll(strategy.getNodeIds());
            }
        }

        List<NodeInfo> nodes = nodeRegistryService.listNodes();
        List<PluginGrayNodeStatus> result = nodes.stream().map(n -> {
            PluginGrayNodeStatus dto = new PluginGrayNodeStatus();
            dto.setNodeId(n.getNodeId());
            dto.setAppName(n.getAppName());
            dto.setIp(n.getIp());
            dto.setPort(n.getPort());
            dto.setLastHeartbeatTime(n.getLastHeartbeatTime());
            dto.setStableVersion(stableVersion);
            dto.setGrayVersion(grayVersion);
            String current = n.getPlugins() != null ? n.getPlugins().get(pluginId) : null;
            dto.setCurrentVersion(current);
            dto.setInGray(grayNodeIds.contains(n.getNodeId()));
            String lastError =
                n.getPluginErrors() != null ? n.getPluginErrors().get(pluginId) : null;
            dto.setLastError(lastError);
            return dto;
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    /**
     * 为指定插件设置灰度节点列表
     * <p>
     * 前端可先调用 listGrayNodes 获取节点列表与当前状态，在界面上勾选后， 将选中节点的 nodeId 列表连同灰度版本提交到该接口。
     */
    @PostMapping("/gray/nodes")
    public ApiResponse<PluginReleaseConfig> updateGrayNodes(@RequestParam("pluginId") String pluginId,
        @RequestBody PluginGrayNodesRequest request) {
        PluginDeployRequest deploy = new PluginDeployRequest();
        deploy.setVersion(request.getVersion());
        deploy.setMode("GRAY");
        deploy.setGrayType("NODE");
        deploy.setGrayNodeIds(request.getNodeIds());
        deploy.setOperator(request.getOperator());
        deploy.setRemark(request.getRemark());
        return ApiResponse.success(pluginAdminService.deploy(pluginId, deploy));
    }

    /**
     * 下载指定插件版本的 JAR 文件
     * <p>
     * 为减少路径变量的使用，改为通过查询参数传递 pluginId 与 version。
     */
    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> download(@RequestParam("pluginId") String pluginId,
        @RequestParam("version") String version) {
        File jar = pluginAdminService.getPluginJarFile(pluginId, version);
        FileSystemResource resource = new FileSystemResource(jar);

        String fileName = pluginId + "-" + version + ".jar";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(jar.length())
            .body(resource);
    }

    /**
     * 获取插件当前发布配置
     */
    @GetMapping("/release")
    public ApiResponse<PluginReleaseConfig> getRelease(@RequestParam("pluginId") String pluginId) {
        return ApiResponse.success(pluginAdminService.getRelease(pluginId));
    }

    /**
     * 发布 / 灰度 / 回滚
     */
    @PostMapping("/deploy")
    public ApiResponse<PluginReleaseConfig> deploy(@RequestParam("pluginId") String pluginId,
        @RequestBody PluginDeployRequest request) {
        return ApiResponse.success(pluginAdminService.deploy(pluginId, request));
    }

    /**
     * 删除插件：清理仓库中的所有版本，并将发布配置标记为已移除
     */
    @PostMapping("/remove")
    public ApiResponse<PluginReleaseConfig> remove(@RequestParam("pluginId") String pluginId) {
        return ApiResponse.success(pluginAdminService.removePlugin(pluginId));
    }

    /**
     * 为节点返回当前插件发布快照
     */
    @GetMapping("/snapshot")
    public ApiResponse<PluginSnapshot> snapshot() {
        return ApiResponse.success(pluginAdminService.getSnapshot());
    }

    /**
     * 简单列出所有插件及其版本列表
     */
    @GetMapping("list")
    public ApiResponse<Map<String, List<PluginVersionInfo>>> listAllPlugins() {
        return ApiResponse.success(pluginAdminService.listAllPlugins());
    }

    /**
     * 查询指定插件在各个节点上的版本状态， 便于运维查看“目标版本 vs 当前版本”和是否存在未同步 / 启用失败的节点。
     */
    @GetMapping("/nodes/status")
    public ApiResponse<List<NodePluginStatus>> listPluginNodeStatus(@RequestParam("pluginId") String pluginId) {
        PluginReleaseConfig release = pluginAdminService.getRelease(pluginId);

        List<NodePluginStatus> result = new ArrayList<>();
        List<NodeInfo> nodes = nodeRegistryService.listNodes();
        for (NodeInfo n : nodes) {
            NodePluginStatus dto = new NodePluginStatus();
            dto.setNodeId(n.getNodeId());
            dto.setAppName(n.getAppName());
            dto.setIp(n.getIp());
            dto.setPort(n.getPort());
            dto.setLastHeartbeatTime(n.getLastHeartbeatTime());

            String current = n.getPlugins() != null ? n.getPlugins().get(pluginId) : null;
            dto.setCurrentVersion(current);
            String lastError =
                n.getPluginErrors() != null ? n.getPluginErrors().get(pluginId) : null;
            dto.setLastError(lastError);

            if (release == null) {
                dto.setTargetVersion(null);
                dto.setTargetType("NONE");
                dto.setSyncStatus("NO_TARGET");
            } else {
                String targetVersion = selectTargetVersionForNode(release, n.getNodeId());
                dto.setTargetVersion(targetVersion);

                String targetType;
                if (targetVersion == null) {
                    targetType = "NONE";
                } else if (release.getGrayVersion() != null
                    && release.getGrayVersion().equals(targetVersion)) {
                    targetType = "GRAY";
                } else {
                    targetType = "STABLE";
                }
                dto.setTargetType(targetType);

                if (targetVersion == null) {
                    dto.setSyncStatus("NO_TARGET");
                } else if (current == null || current.isEmpty()) {
                    dto.setSyncStatus("NOT_INSTALLED");
                } else if (targetVersion.equals(current)) {
                    dto.setSyncStatus("SYNCED");
                } else {
                    dto.setSyncStatus("OUT_OF_SYNC");
                }
            }

            result.add(dto);
        }
        return ApiResponse.success(result);
    }

    /**
     * 在服务端按与节点一致的规则，计算指定插件在某个节点上的目标版本。
     * <p>
     * 当前仅支持 GrayStrategy.type = NODE： - grayVersion 为空时，仅下发 stableVersion； - type=NODE 时，nodeId 在 nodeIds 列表中下发
     * grayVersion，否则下发 stableVersion。
     */
    private String selectTargetVersionForNode(PluginReleaseConfig cfg, String nodeId) {
        String stable = cfg.getStableVersion();
        String gray = cfg.getGrayVersion();
        GrayStrategy strategy = cfg.getGrayStrategy();

        if (gray == null || gray.trim().isEmpty()) {
            return stable;
        }
        if (strategy == null || strategy.getType() == null) {
            return stable;
        }

        String type = strategy.getType().toUpperCase();
        if ("NODE".equals(type) && strategy.getNodeIds() != null && !strategy.getNodeIds().isEmpty()) {
            if (strategy.getNodeIds().contains(nodeId)) {
                return gray;
            }
            return stable;
        }
        return stable;
    }
}
