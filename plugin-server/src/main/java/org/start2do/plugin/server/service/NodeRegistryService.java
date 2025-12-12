package org.start2do.plugin.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.start2do.plugin.config.PluginSystemProperties;
import org.start2do.plugin.server.model.NodeInfo;
import org.start2do.plugin.server.web.dto.NodeRegisterRequest;

/**
 * 基于文件的节点注册与心跳管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeRegistryService {

    private final PluginSystemProperties pluginSystemProperties;
    private final ObjectMapper objectMapper;

    /**
     * 心跳时间统一格式：
     * - 模式：yyyy-MM-dd HH:mm:ss
     * - 时区：系统默认时区
     */
    private static final DateTimeFormatter HEARTBEAT_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 节点信息存储目录：{storagePath}/cluster/nodes
     */
    private File getNodeDir() {
        File root = new File(pluginSystemProperties.getServer().getStoragePath());
        File dir = new File(root, "cluster/nodes");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("无法创建节点存储目录: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private File nodeFile(String nodeId) {
        return new File(getNodeDir(), nodeId + ".json");
    }

    /**
     * 注册或更新节点信息
     */
    public NodeInfo register(NodeRegisterRequest request) {
        if (request == null || request.getNodeId() == null || request.getNodeId().trim().isEmpty()) {
            throw new IllegalArgumentException("nodeId 不能为空");
        }
        NodeInfo info = new NodeInfo();
        info.setNodeId(request.getNodeId());
        info.setAppName(request.getAppName());
        info.setIp(request.getIp());
        info.setPort(request.getPort());
        info.setTags(request.getTags());
        info.setPlugins(request.getPlugins());
        info.setPluginErrors(request.getPluginErrors());
        // 以系统默认时区记录心跳时间，格式为 yyyy-MM-dd HH:mm:ss
        info.setLastHeartbeatTime(LocalDateTime.now().format(HEARTBEAT_TIME_FORMATTER));

        File file = nodeFile(info.getNodeId());
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, info);
        } catch (IOException e) {
            log.error("写入节点信息失败, nodeId={}", info.getNodeId(), e);
            throw new IllegalStateException("写入节点信息失败:" + e.getMessage());
        }
        return info;
    }

    /**
     * 查询所有已注册节点
     */
    public List<NodeInfo> listNodes() {
        File dir = getNodeDir();
        File[] files = dir.listFiles();
        List<NodeInfo> result = new ArrayList<>();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) {
                continue;
            }
            try {
                NodeInfo info = objectMapper.readValue(file, NodeInfo.class);
                if (info != null && info.getNodeId() != null) {
                    result.add(info);
                }
            } catch (IOException e) {
                log.warn("读取节点信息失败, file={}", file.getAbsolutePath(), e);
            }
        }
        // 可根据最后心跳时间排序，便于运维查看
        result.sort((a, b) -> {
            Instant t1 = parseHeartbeatTime(a.getLastHeartbeatTime());
            Instant t2 = parseHeartbeatTime(b.getLastHeartbeatTime());
            return t2.compareTo(t1);
        });
        return result;
    }

    /**
     * 将心跳时间字符串解析为 Instant，兼容历史数据格式：
     * - 新格式：yyyy-MM-dd HH:mm:ss（视为系统默认时区的本地时间）
     * - 旧格式：ISO-8601 Instant 字符串
     */
    private Instant parseHeartbeatTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Instant.EPOCH;
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(value, HEARTBEAT_TIME_FORMATTER);
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception ignore) {
            try {
                return Instant.parse(value);
            } catch (Exception e) {
                log.warn("无法解析 lastHeartbeatTime 字段, value={}", value, e);
                return Instant.EPOCH;
            }
        }
    }
}
