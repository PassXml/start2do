package org.start2do.plugin.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.plugin.api.dto.GrayStrategy;
import org.start2do.plugin.api.dto.PluginReleaseConfig;
import org.start2do.plugin.api.dto.PluginSnapshot;
import org.start2do.plugin.config.PluginSystemProperties;
import org.start2do.plugin.server.model.PluginVersionInfo;
import org.start2do.plugin.server.web.dto.PluginDeployRequest;

/**
 * 基于文件的插件版本与发布管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginAdminService {

    private final PluginSystemProperties pluginSystemProperties;
    private final ObjectMapper objectMapper;

    /**
     * 发布历史 operateTime 统一格式：
     * - 模式：yyyy-MM-dd HH:mm:ss
     * - 时区：系统默认时区（避免硬编码为 UTC，方便本地排查）
     */
    private static final DateTimeFormatter OPERATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    /**
     * 插件仓库目录：{storagePath}/repository/{pluginId}/{version}/plugin.jar
     */
    private File getRepositoryRoot() {
        File root = new File(pluginSystemProperties.getServer().getStoragePath());
        File dir = new File(root, "repository");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("无法创建插件仓库目录: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private File getPluginVersionDir(String pluginId, String version) {
        File root = getRepositoryRoot();
        return new File(new File(root, pluginId + "/versions"), version);
    }

    /**
     * 发布配置目录：{storagePath}/releases
     */
    private File getReleaseDir() {
        // 与仓库目录一样，统一从 plugin.server.storage-path 读取根目录
        File root = new File(pluginSystemProperties.getServer().getStoragePath());
        File dir = new File(root, "releases");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("无法创建发布配置目录: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private File getReleaseFile(String pluginId) {
        return new File(getReleaseDir(), pluginId + ".json");
    }

    /**
     * 上传插件新版本时，从插件 JAR 的 Manifest 中解析出 pluginId 和 version
     */
    private PluginJarMeta resolveJarMeta(File jarFile, String originalName) {
        try (FileInputStream fis = new FileInputStream(jarFile); JarInputStream jis = new JarInputStream(fis)) {
            Manifest manifest = jis.getManifest();
            if (manifest != null) {
                Attributes attrs = manifest.getMainAttributes();
                String pluginId = attrs.getValue("Plugin-Id");
                String version = attrs.getValue("Plugin-Version");

                if (pluginId != null && !pluginId.trim().isEmpty() && version != null && !version.trim().isEmpty()) {
                    return new PluginJarMeta(pluginId.trim(), version.trim());
                }
            }
            log.warn("插件 JAR Manifest 中缺少 Plugin-Id 或 Plugin-Version, originalName={}", originalName);
            throw new IllegalArgumentException(
                "插件 Jar 缺少 Manifest 中的 Plugin-Id 或 Plugin-Version，请检查构建配置");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            log.warn("解析插件 JAR Manifest 失败, originalName={}, msg={}", originalName, e.getMessage(), e);
            throw new IllegalArgumentException(
                "解析插件 Jar Manifest 失败，请检查是否包含有效的 Plugin-Id 与 Plugin-Version", e);
        }
    }

    /**
     * 校验从 Manifest 中解析出的 pluginId / version，避免目录遍历等问题
     */
    private void validatePluginIdAndVersion(String pluginId, String version) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("插件 Manifest 中的 Plugin-Id 不能为空");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("插件 Manifest 中的 Plugin-Version 不能为空");
        }
        String id = pluginId.trim();
        String ver = version.trim();
        if (!id.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException("插件 Manifest 中的 Plugin-Id 不符合命名规则: " + id);
        }
        if (!ver.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException("插件 Manifest 中的 Plugin-Version 不符合命名规则: " + ver);
        }
    }

    /**
     * 上传插件新版本，仅保存文件与版本元数据，不改变当前发布状态
     */
    public PluginVersionInfo upload(MultipartFile file, String changelog) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.endsWith(".jar")) {
            throw new IllegalArgumentException("只支持上传 .jar 文件");
        }

        // 先写入系统临时目录下的临时文件，再从 JAR 中解析 Plugin-Id / Plugin-Version
        // 参考 PluginFileService 的实现，避免在相对路径下由容器 Part.write 解析导致路径错误
        File tmp = File.createTempFile("plugin-upload-", ".jar");
        try {
            file.transferTo(tmp);
            PluginJarMeta meta = resolveJarMeta(tmp, originalName);
            validatePluginIdAndVersion(meta.getPluginId(), meta.getVersion());

            // 若路径中的 pluginId 与 Manifest 中的不一致，直接拒绝，避免误操作
            File versionDir = getPluginVersionDir(meta.getPluginId(), meta.getVersion());
            if (!versionDir.exists() && !versionDir.mkdirs()) {
                throw new IllegalStateException("无法创建插件版本目录: " + versionDir.getAbsolutePath());
            }
            File target = new File(versionDir, "plugin.jar");

            // 使用临时文件 + 原子移动，避免并发写入导致的半写入文件；
            // 若文件系统或跨盘符不支持原子移动，则退化为普通移动，兼容 Windows 等环境
            try {
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                log.warn("当前文件系统不支持原子移动, 将回退为普通移动, tmp={}, target={}", tmp.getAbsolutePath(),
                    target.getAbsolutePath());
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            PluginVersionInfo info = new PluginVersionInfo();
            info.setPluginId(meta.getPluginId());
            info.setVersion(meta.getVersion());
            info.setStatus("DRAFT");
            info.setJarPath(storageRelativePath(target));
            info.setChecksum(null);
            info.setChangelog(changelog);

            File manifestFile = new File(versionDir, "manifest.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(manifestFile, info);
            return info;
        } finally {
            // 双保险：若前面出现异常，确保临时文件被清理
            if (tmp.exists()) {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }
        }
    }

    private String storageRelativePath(File file) {
        File root = new File(pluginSystemProperties.getServer().getStoragePath()).getAbsoluteFile();
        return root.toPath().relativize(file.getAbsoluteFile().toPath()).toString().replace(File.separatorChar, '/');
    }

    /**
     * 删除插件：清理仓库中的所有版本，并将发布配置标记为已移除（保留历史）
     */
    public PluginReleaseConfig removePlugin(String pluginId) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("pluginId 不能为空");
        }
        String normalizedId = pluginId.trim();
        if (!normalizedId.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException("pluginId 不符合命名规则: " + normalizedId);
        }

        // 1. 删除仓库中该插件的所有版本目录
        File repoRoot = getRepositoryRoot();
        File pluginRoot = new File(repoRoot, normalizedId);
        if (pluginRoot.exists()) {
            deleteDirectoryRecursively(pluginRoot);
        }

        // 2. 更新发布配置：清空当前发布版本，追加“REMOVE”历史记录
        File releaseFile = getReleaseFile(normalizedId);
        PluginReleaseConfig cfg = loadReleaseInternal(releaseFile);
        if (cfg == null) {
            cfg = new PluginReleaseConfig();
            cfg.setPluginId(normalizedId);
        }

        PluginReleaseConfig.ReleaseHistoryItem history = new PluginReleaseConfig.ReleaseHistoryItem();
        history.setFromVersion(cfg.getStableVersion());
        history.setToVersion(null);
        history.setOperation("REMOVE");
        history.setOperateTime(OPERATE_TIME_FORMATTER.format(Instant.now()));
        history.setOperator(null);
        history.setRemark("删除插件及所有版本");
        appendHistory(cfg, history);

        cfg.setStableVersion(null);
        cfg.setGrayVersion(null);
        cfg.setGrayStrategy(null);

        writeReleaseWithLock(releaseFile, cfg);
        return cfg;
    }

    /**
     * 发布 / 灰度 / 回滚
     */
    public PluginReleaseConfig deploy(String pluginId, PluginDeployRequest request) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("pluginId 不能为空");
        }
        if (request.getVersion() == null || request.getVersion().trim().isEmpty()) {
            throw new IllegalArgumentException("version 不能为空");
        }
        String mode = request.getMode() == null ? "FULL" : request.getMode().toUpperCase();

        File releaseFile = getReleaseFile(pluginId);
        PluginReleaseConfig cfg = loadReleaseInternal(releaseFile);
        if (cfg == null) {
            cfg = new PluginReleaseConfig();
            cfg.setPluginId(pluginId);
        }

        PluginReleaseConfig.ReleaseHistoryItem history = new PluginReleaseConfig.ReleaseHistoryItem();
        history.setFromVersion(cfg.getStableVersion());
        history.setToVersion(request.getVersion());
        history.setOperateTime(OPERATE_TIME_FORMATTER.format(Instant.now()));
        history.setOperator(request.getOperator());
        history.setRemark(request.getRemark());

        switch (mode) {
            case "FULL":
                cfg.setStableVersion(request.getVersion());
                cfg.setGrayVersion(null);
                cfg.setGrayStrategy(null);
                history.setOperation("DEPLOY");
                break;
            case "GRAY":
                GrayStrategy strategy = new GrayStrategy();
                strategy.setType(request.getGrayType());
                strategy.setValue(request.getGrayValue());
                // 显式指定需要下发灰度版本的节点列表
                strategy.setNodeIds(request.getGrayNodeIds());
                cfg.setGrayVersion(request.getVersion());
                cfg.setGrayStrategy(strategy);
                history.setOperation("GRAY");
                break;
            case "ROLLBACK":
                cfg.setStableVersion(request.getVersion());
                cfg.setGrayVersion(null);
                cfg.setGrayStrategy(null);
                history.setOperation("ROLLBACK");
                break;
            default:
                throw new IllegalArgumentException("不支持的发布模式: " + mode);
        }

        appendHistory(cfg, history);
        writeReleaseWithLock(releaseFile, cfg);
        return cfg;
    }

    /**
     * 追加发布历史记录，最多保留最近 10 条，超出时丢弃最早的记录
     */
    private void appendHistory(PluginReleaseConfig cfg, PluginReleaseConfig.ReleaseHistoryItem history) {
        if (cfg.getHistory() == null) {
            cfg.setHistory(new java.util.ArrayList<PluginReleaseConfig.ReleaseHistoryItem>());
        }
        cfg.getHistory().add(history);
        List<PluginReleaseConfig.ReleaseHistoryItem> list = cfg.getHistory();
        int maxSize = 10;
        while (list.size() > maxSize) {
            // 始终移除最早的一条记录
            list.remove(0);
        }
    }

    /**
     * 递归删除目录/文件，删除失败时仅记录日志不抛出异常
     */
    private void deleteDirectoryRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectoryRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            log.warn("删除插件目录或文件失败, path={}", file.getAbsolutePath());
        }
    }

    private PluginReleaseConfig loadReleaseInternal(File releaseFile) {
        if (!releaseFile.exists()) {
            return null;
        }
        try {
            return objectMapper.readValue(releaseFile, PluginReleaseConfig.class);
        } catch (IOException e) {
            log.warn("读取发布配置失败, file={}", releaseFile.getAbsolutePath(), e);
            return null;
        }
    }

    private void writeReleaseWithLock(File file, PluginReleaseConfig cfg) {
        // 简单文件锁，避免多实例并发写入导致文件损坏
        try (FileOutputStream fos = new FileOutputStream(file);
            FileChannel channel = fos.getChannel();
            FileLock lock = channel.lock()) {
            byte[] bytes =
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(cfg);
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            log.error("写入发布配置失败, file={}", file.getAbsolutePath(), e);
            throw new IllegalStateException("写入发布配置失败:" + e.getMessage());
        }
    }

    /**
     * 查询单个插件发布配置
     */
    public PluginReleaseConfig getRelease(String pluginId) {
        return loadReleaseInternal(getReleaseFile(pluginId));
    }

    /**
     * 构建所有插件的发布快照
     */
    public PluginSnapshot getSnapshot() {
        PluginSnapshot snapshot = new PluginSnapshot();
        File dir = getReleaseDir();
        File[] files = dir.listFiles();
        if (files == null) {
            return snapshot;
        }
        List<PluginReleaseConfig> releases = new ArrayList<>();
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) {
                continue;
            }
            PluginReleaseConfig cfg = loadReleaseInternal(file);
            if (cfg != null && cfg.getPluginId() != null) {
                releases.add(cfg);
            }
        }
        snapshot.setReleases(releases);
        return snapshot;
    }

    /**
     * 列出某插件所有版本信息
     */
    public List<PluginVersionInfo> listVersions(String pluginId) {
        File repoRoot = getRepositoryRoot();
        File pluginRoot = new File(repoRoot, pluginId + "/versions");
        if (!pluginRoot.exists() || !pluginRoot.isDirectory()) {
            return new ArrayList<>();
        }
        File[] versionDirs = pluginRoot.listFiles();
        List<PluginVersionInfo> result = new ArrayList<>();
        if (versionDirs == null) {
            return result;
        }
        for (File dir : versionDirs) {
            if (!dir.isDirectory()) {
                continue;
            }
            File manifest = new File(dir, "manifest.json");
            if (!manifest.exists()) {
                continue;
            }
            try {
                PluginVersionInfo info =
                    objectMapper.readValue(manifest, PluginVersionInfo.class);
                if (info != null) {
                    result.add(info);
                }
            } catch (IOException e) {
                log.warn("读取版本元数据失败, file={}", manifest.getAbsolutePath(), e);
            }
        }
        return result;
    }

    /**
     * 返回所有插件 ID -> 最新版本列表，便于简单查看
     */
    public Map<String, List<PluginVersionInfo>> listAllPlugins() {
        File repoRoot = getRepositoryRoot();
        File[] pluginDirs = repoRoot.listFiles();
        Map<String, List<PluginVersionInfo>> result = new HashMap<>();
        if (pluginDirs == null) {
            return result;
        }
        for (File pluginDir : pluginDirs) {
            if (!pluginDir.isDirectory()) {
                continue;
            }
            String pluginId = pluginDir.getName();
            result.put(pluginId, listVersions(pluginId));
        }
        return result;
    }

    /**
     * 获取指定插件版本的 JAR 文件
     */
    public File getPluginJarFile(String pluginId, String version) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("pluginId 不能为空");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("version 不能为空");
        }
        File versionDir = getPluginVersionDir(pluginId, version);
        File jar = new File(versionDir, "plugin.jar");
        if (!jar.exists() || !jar.isFile()) {
            throw new IllegalStateException("未找到插件版本 JAR 文件: pluginId=" + pluginId + ", version=" + version);
        }
        return jar;
    }

    /**
     * 简单封装从 JAR 中解析的插件元数据
     */
    private static class PluginJarMeta {

        private final String pluginId;
        private final String version;

        private PluginJarMeta(String pluginId, String version) {
            this.pluginId = pluginId;
            this.version = version;
        }

        public String getPluginId() {
            return pluginId;
        }

        public String getVersion() {
            return version;
        }
    }
}
