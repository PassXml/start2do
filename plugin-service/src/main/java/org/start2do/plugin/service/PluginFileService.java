package org.start2do.plugin.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.plugin.api.dto.PluginJarMeta;
import org.start2do.plugin.api.util.PluginJarMetaUtils;
import org.start2do.plugin.config.PluginSystemProperties;
import org.start2do.plugin.dto.PluginInfo;
import org.start2do.plugin.env.PluginConfigRegistry;
import org.start2do.plugin.handle.PluginControllerMappingConflictException;

/**
 * 插件文件管理服务
 * <p>
 * 约定： - 启用：{name}.jar - 禁用：{name}.jar.disable
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginFileService {

    private final PluginSystemProperties pluginSystemProperties;
    private final PluginConfigRegistry pluginConfigRegistry;
    private final Environment environment;
    private final PluginRuntimeRegistry pluginRuntimeRegistry;

    /**
     * PF4J 插件管理器
     * <p>
     * 通过 plugin-bridge 模块自动装配，允许在启用/停用插件时实时通知 PF4J。 当未启用 eip.pf4j.enabled 时，该 Bean 不存在，相关逻辑自动降级为仅操作文件。
     */
    private final PluginManager pluginManager;

    /**
     * 获取插件根目录，不存在时自动创建
     */
    private File getRootDir() {
        String path = pluginSystemProperties.getRuntime().getStoragePath();
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalStateException("插件存储目录未配置");
        }
        // 将配置路径统一转换为绝对路径，避免在外置 Tomcat 等环境下出现相对路径
        // 被解析到临时目录导致文件无法写入的问题
        File dir = Paths.get(path).toAbsolutePath().normalize().toFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("无法创建插件存储目录: " + dir.getAbsolutePath());
            }
        }
        if (!dir.isDirectory()) {
            throw new IllegalStateException("插件存储路径不是目录: " + dir.getAbsolutePath());
        }
        return dir;
    }

    /**
     * 校验插件基础名称是否合法（不包含路径等）
     */
    private void validateName(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            throw new IllegalArgumentException("插件名称不能为空");
        }
        String name = baseName.trim();
        if (name.contains("..") || name.contains("/") || name.contains("\\") || name.contains(File.separator)) {
            throw new IllegalArgumentException("插件名称包含非法字符: " + baseName);
        }
        if (!name.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException("插件名称不符合命名规则: " + baseName);
        }
    }

    /**
     * 获取启用状态文件：{name}.jar
     */
    private File enabledFile(String baseName) {
        if (baseName.endsWith(".jar.disable")) {
            baseName = baseName.substring(0, baseName.length() - ".jar.disable".length());
        }
        if (baseName.endsWith(".jar")) {
            baseName = baseName.substring(0, baseName.length() - ".jar".length());
        }
        return new File(getRootDir(), baseName + ".jar");
    }

    /**
     * 获取禁用状态文件：{name}.jar.disable
     */
    private File disabledFile(String baseName) {
        if (baseName.endsWith(".jar.disable")) {
            return new File(getRootDir(), baseName);
        }
        if (baseName.endsWith(".jar")) {
            baseName = baseName.substring(0, baseName.length() - ".jar".length());
        }
        return new File(getRootDir(), baseName + ".jar.disable");
    }

    private boolean isRunStateByFilePath(Path filePath) {
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            if (plugin.getPluginPath().toAbsolutePath().normalize().equals(filePath)) {
                if (plugin.getPluginState() == PluginState.STARTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 尝试通过 PF4J 启用插件
     *
     * @param enabledJar 启用状态的插件文件
     */
    private String startPluginIfPossible(File enabledJar) {
        if (pluginManager == null) {
            // 未启用 PF4J 桥接，跳过运行时启用
            return null;
        }
        String pluginId = null;
        try {
            // 先解析 pluginId，便于在“同 pluginId 已加载旧版本”场景先卸载清理，避免 loadPlugin 失败后误删新包
            pluginId = tryResolvePluginIdFromJar(enabledJar);
            if (pluginId != null) {
                PluginWrapper existing = getPluginInfoById(pluginId);
                if (existing != null) {
                    Path existingPath = existing.getPluginPath() == null
                        ? null
                        : existing.getPluginPath().toAbsolutePath().normalize();
                    Path newPath = enabledJar.toPath().toAbsolutePath().normalize();
                    if (existing.getPluginState() == PluginState.STARTED && newPath.equals(existingPath)) {
                        // 若插件文件已被外部更新（同路径覆盖），则需要“先卸载再重新加载”，否则仍会使用旧 ClassLoader
                        if (pluginRuntimeRegistry.hasJarFingerprintChanged(pluginId, enabledJar)) {
                            log.info("检测到插件文件已更新，将重启插件以生效: pluginId={}, path={}",
                                pluginId, enabledJar.getAbsolutePath());
                            stopPluginIfPossible(pluginId);
                        } else {
                            // 若未记录过指纹（例如插件不是通过 PluginFileService 启动），先记录基线，避免后续无法检测变化
                            pluginRuntimeRegistry.recordJarFingerprint(pluginId, enabledJar);
                            log.info("PF4J 动态启用插件: 已处于运行状态, pluginId={}, path={}", pluginId,
                                enabledJar.getAbsolutePath());
                            return PluginState.STARTED.toString();
                        }
                    }
                    stopPluginIfPossible(pluginId);
                }
            }

            // 若插件对应的 JAR 已处于运行状态，则直接返回，避免重复加载与误删文件
            if (isRunStateByFilePath(enabledJar.toPath())) {
                log.info("PF4J 动态启用插件: 已处于运行状态, path={}", enabledJar.getAbsolutePath());
                return PluginState.STARTED.toString();
            }
            // 若插件尚未加载，则按文件路径动态加载
            String loadedPluginId = pluginManager.loadPlugin(enabledJar.toPath());
            if (loadedPluginId != null) {
                pluginId = loadedPluginId;
            }
            // 在真正 startPlugin 之前预加载插件 application*.yml，便于插件在 Plugin#start 阶段读取配置
            try {
                PluginWrapper wrapper = pluginId == null ? null : pluginManager.getPlugin(pluginId);
                if (wrapper != null) {
                    pluginConfigRegistry.loadOrReload(pluginId, wrapper.getPluginClassLoader(), environment.getActiveProfiles());
                }
            } catch (Exception ex) {
                log.warn("插件 {} 预加载 application*.yml 失败(忽略继续)", pluginId, ex);
            }
            PluginState plugin = pluginManager.startPlugin(pluginId);
            log.info("PF4J 动态加载并启动插件成功, baseName={}, pluginId={}", enabledJar.getAbsolutePath(), pluginId);
            // 记录本次启动对应文件指纹（用于后续 enable 时判断是否发生“同路径覆盖更新”）
            pluginRuntimeRegistry.recordJarFingerprint(pluginId, enabledJar);
            return Optional.ofNullable(plugin).map(PluginState::toString).orElseGet(() -> "ERROR");
        } catch (Exception e) {
            // 启用失败时，卸载插件并删除本地 JAR，避免子节点残留无效插件文件
            cleanupFailedPluginStart(enabledJar, pluginId, e);
            // 如果是 Controller 路由冲突，向上抛出详细异常信息，便于上传/启用接口直接返回
            PluginControllerMappingConflictException conflict = findControllerConflictException(e);
            if (conflict != null) {
                throw conflict;
            }
        }
        return PluginState.UNLOADED.toString();
    }

    /**
     * 插件启用失败后的清理逻辑：尽量卸载插件并删除对应 JAR 文件
     */
    private void cleanupFailedPluginStart(File enabledJar, String pluginId, Exception cause) {
        try {
            if (pluginId != null) {
                try {
                    pluginManager.stopPlugin(pluginId);
                } catch (Exception ignore) {
                    // 忽略停止异常，继续尝试卸载与删除文件
                }
                try {
                    pluginManager.unloadPlugin(pluginId);
                } catch (Exception ignore) {
                    // 忽略卸载异常
                }
                try {
                    pluginConfigRegistry.unload(pluginId);
                } catch (Exception ignore) {
                    // 忽略卸载配置异常
                }
                pluginRuntimeRegistry.clear(pluginId);
            }
        } catch (Exception ignore) {
            // 避免清理过程中异常打断后续逻辑
        }

        if (enabledJar != null && enabledJar.exists()) {
            boolean deleted = enabledJar.delete();
            if (!deleted) {
                log.warn("插件启用失败后删除本地 JAR 文件失败, path={}", enabledJar.getAbsolutePath());
            } else {
                log.info("插件启用失败后已删除本地 JAR 文件, path={}", enabledJar.getAbsolutePath());
            }
        }
        log.warn("PF4J 启用插件失败, path={}, msg={}", enabledJar != null ? enabledJar.getAbsolutePath() : "<null>",
            cause.getMessage(), cause);
    }

    /**
     * 从异常链中查找插件 Controller 路由冲突异常
     */
    private PluginControllerMappingConflictException findControllerConflictException(Throwable e) {
        while (e != null) {
            if (e instanceof PluginControllerMappingConflictException) {
                return (PluginControllerMappingConflictException) e;
            }
            e = e.getCause();
        }
        return null;
    }

    public PluginWrapper getPluginInfoById(String pluginId) {
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            if (plugin.getPluginId().equals(pluginId)) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * 尝试通过 PF4J 停用插件（停止并卸载）
     *
     * @param pluginId 插件基础名称（约定与 pluginId 一致）
     */
    private String stopPluginIfPossible(String pluginId) {
        if (pluginManager == null) {
            return null;
        }
        String jarPath = null;
        try {
            PluginWrapper info = getPluginInfoById(pluginId);
            if (info != null) {
                jarPath = info.getPluginPath().getFileName().toString();
                pluginManager.stopPlugin(pluginId);
                pluginManager.unloadPlugin(pluginId);
                pluginRuntimeRegistry.clear(pluginId);
                log.info("PF4J 停用并卸载插件成功, pluginId={}", pluginId);
                return jarPath;
            }
        } catch (Exception e) {
            log.warn("PF4J 停用插件失败, baseName={}, msg={}", pluginId, e.getMessage(), e);
        }
        return jarPath;
    }

    /**
     * 上传插件文件
     *
     * @param file   上传的 jar 包
     * @param enable 是否立即启用
     */
    public String upload(MultipartFile file, boolean enable) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.endsWith(".jar")) {
            throw new IllegalArgumentException("只支持上传 .jar 文件");
        }

        // 先写入临时文件，再从 JAR 中解析插件元数据（plugin.id / plugin.version 或 Manifest 中的 Plugin-Id / Plugin-Version），统一命名为 pluginId-version.jar
        File tmp = File.createTempFile("plugin-upload-", ".jar");
        try {
            file.transferTo(tmp);

            PluginJarMeta dto = resolveStandardJarName(tmp, originalName);
            validateName(dto.getFileName());

            File target = enable ? enabledFile(dto.getFileName()) : disabledFile(dto.getFileName());
            // 先停用并卸载旧版本，再清理同一 pluginId 的历史包（避免多版本残留）
            if (pluginManager != null) {
                stopPluginIfPossible(dto.getPluginId());
            }
            cleanupLocalPluginFilesByPluginId(dto.getPluginId(), target.toPath());

            getRootDir();
            // 使用原子移动，避免出现半写入文件；若文件系统不支持原子移动（如跨磁盘），则降级为普通移动
            try {
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                log.warn("当前文件系统不支持原子移动, 将回退为普通移动, tmp={}, target={}", tmp.getAbsolutePath(),
                    target.getAbsolutePath());
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 若上传后立即启用，则通知 PF4J 动态加载插件
            if (enable) {
                return startPluginIfPossible(target);
            }
            return null;
        } finally {
            // 双保险：若前面出现异常，确保临时文件被清理
            if (tmp.exists()) {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }
        }
    }

    /**
     * 通过本地 JAR 文件上传插件（供内部使用，例如 plugin-client 从管理端下载后写入本地）
     *
     * @param jarFile      本地 JAR 文件
     * @param enable       是否立即启用
     * @param originalName 原始文件名（用于日志与异常提示，可为 jarFile.getName()）
     */
    public String upload(File jarFile, boolean enable, String originalName) throws IOException {
        if (jarFile == null || !jarFile.isFile()) {
            throw new IllegalArgumentException("上传文件不存在或不是普通文件");
        }
        if (originalName == null || !originalName.endsWith(".jar")) {
            originalName = jarFile.getName();
        }

        PluginJarMeta dto = resolveStandardJarName(jarFile, originalName);
        validateName(dto.getFileName());

        File target = enable ? enabledFile(dto.getFileName()) : disabledFile(dto.getFileName());
        if (pluginManager != null) {
            stopPluginIfPossible(dto.getPluginId());
        }
        cleanupLocalPluginFilesByPluginId(dto.getPluginId(), target.toPath());

        getRootDir();
        Files.copy(jarFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

        if (enable) {
            return startPluginIfPossible(target);
        }
        return null;
    }

    /**
     * 根据插件 Jar 元数据推导标准文件名：pluginId-version.jar。
     * <p>
     * 优先从 plugin.properties 中读取 plugin.id / plugin.version，
     * 若不存在则回退到 Manifest 中的 Plugin-Id / Plugin-Version。
     */
    private PluginJarMeta resolveStandardJarName(File jarFile, String originalName) {
        try {
            return PluginJarMetaUtils.resolveFromJar(jarFile, originalName);
        } catch (IllegalArgumentException e) {
            // 统一在此处加日志，避免各处重复实现解析逻辑
            log.warn("解析插件 JAR 元数据失败, originalName={}, msg={}", originalName, e.getMessage(), e);
            throw e;
        }
    }

    private String tryResolvePluginIdFromJar(File jarFile) {
        if (jarFile == null || !jarFile.isFile()) {
            return null;
        }
        try {
            PluginJarMeta meta = resolveStandardJarName(jarFile, jarFile.getName());
            return meta == null ? null : meta.getPluginId();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 清理同一 pluginId 的历史插件包（启用/禁用），避免多版本残留导致“同名类多份/加载来源不一致”。
     * <p>
     * 注意：会保留 keepPath 对应的文件（若传入）。
     */
    private void cleanupLocalPluginFilesByPluginId(String pluginId, Path keepPath) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return;
        }
        File rootDir = getRootDir();
        File[] files = rootDir.listFiles();
        if (files == null) {
            return;
        }
        Path keep = keepPath == null ? null : keepPath.toAbsolutePath().normalize();
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            String name = file.getName();
            if (!name.endsWith(".jar") && !name.endsWith(".jar.disable")) {
                continue;
            }
            Path cur = file.toPath().toAbsolutePath().normalize();
            if (keep != null && keep.equals(cur)) {
                continue;
            }
            try {
                PluginJarMeta meta = resolveStandardJarName(file, name);
                if (meta != null && pluginId.equals(meta.getPluginId())) {
                    Files.deleteIfExists(cur);
                    log.info("已清理历史插件包: pluginId={}, file={}", pluginId, cur);
                }
            } catch (Exception ex) {
                log.debug("清理历史插件包时解析失败(忽略): file={}, msg={}", file.getAbsolutePath(), ex.getMessage());
            }
        }
    }

    private PluginWrapper getPluginInfoByFilePath(File target) {
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            if (plugin.getPluginPath().toAbsolutePath().normalize().equals(target.toPath())) {
                if (plugin.getPluginState() == PluginState.STARTED) {
                    return plugin;
                }
            }
        }
        return null;
    }

    /**
     * 根据插件 ID 在本地存储目录中查找插件文件
     *
     * @param pluginId 插件 ID（来自 Manifest 中的 Plugin-Id）
     * @param enabled  true 表示查找启用文件（*.jar），false 表示查找禁用文件（*.jar.disable）
     */
    private File findPluginFileByPluginId(String pluginId, boolean enabled) {
        File rootDir = getRootDir();
        File[] files = rootDir.listFiles();
        if (files == null) {
            return null;
        }

        File candidate = null;
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            String name = file.getName();
            boolean fileEnabled;
            if (name.endsWith(".jar.disable")) {
                fileEnabled = false;
            } else if (name.endsWith(".jar")) {
                fileEnabled = true;
            } else {
                continue;
            }
            if (fileEnabled != enabled) {
                continue;
            }

            try {
                PluginJarMeta meta = resolveStandardJarName(file, name);
                if (pluginId.equals(meta.getPluginId())) {
                    // 若存在多个版本，优先选择最新修改时间的文件
                    if (candidate == null || file.lastModified() > candidate.lastModified()) {
                        candidate = file;
                    }
                }
            } catch (IllegalArgumentException ex) {
                // 非合法插件包，忽略
                log.debug("按插件 ID 查找插件文件时解析失败, file={}, msg={}", file.getAbsolutePath(), ex.getMessage());
            }
        }

        return candidate;
    }

    /**
     * 启用插件：根据插件 ID，将 {pluginId-xxx}.jar.disable -> {pluginId-xxx}.jar 并通过 PF4J 启动
     */
    public String enable(String pluginId) throws IOException {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("插件 ID 不能为空");
        }
        String normalizedId = pluginId.trim();

        // 1. 优先寻找禁用状态文件（*.jar.disable）
        File disabledFile = findPluginFileByPluginId(normalizedId, false);
        if (disabledFile == null) {
            // 2. 若找不到禁用文件，但存在启用文件，则仅保证 PF4J 中处于启动状态
            File enabledFile = findPluginFileByPluginId(normalizedId, true);
            if (enabledFile != null) {
                return startPluginIfPossible(enabledFile);
            }
            throw new IllegalStateException("未找到插件 ID 为 " + normalizedId + " 的插件文件");
        }

        File enabledTarget = enabledFile(disabledFile.getName());
        if (enabledTarget.exists()) {
            throw new IllegalStateException("启用目标文件已存在，请检查是否重名: " + enabledTarget.getName());
        }

        // 3. 文件层面从 *.jar.disable 切换为 *.jar
        Files.move(disabledFile.toPath(), enabledTarget.toPath(), StandardCopyOption.ATOMIC_MOVE);

        // 清理同一 pluginId 的其他历史包，只保留当前启用目标
        cleanupLocalPluginFilesByPluginId(normalizedId, enabledTarget.toPath());

        // 4. 同步到 PF4J 运行时
        return startPluginIfPossible(enabledTarget);
    }

    /**
     * 停用插件：根据插件 ID，将 {pluginId-xxx}.jar -> {pluginId-xxx}.jar.disable 并通过 PF4J 停用
     */
    public void disable(String pluginId) throws IOException {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("插件 ID 不能为空");
        }
        String normalizedId = pluginId.trim();

        // 1. 先尝试通过 PF4J 停用运行中的插件
        stopPluginIfPossible(normalizedId);

        // 2. 查找启用状态的插件文件并切换为禁用文件
        File enabledFile = findPluginFileByPluginId(normalizedId, true);
        if (enabledFile == null) {
            // 若本地不存在启用文件，则认为已经是停用态，直接返回
            log.info("停用插件时未找到启用状态文件, pluginId={}", normalizedId);
            return;
        }

        File disabledTarget = disabledFile(enabledFile.getName());
        Files.move(enabledFile.toPath(), disabledTarget.toPath(), StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * 删除插件：同时删除启用/禁用文件
     */
    public void delete(String jarFileName) throws IOException {
        validateName(jarFileName);

        File enabled = enabledFile(jarFileName);
        File disabled = disabledFile(jarFileName);

        boolean exists = false;

        // 尽量解析出 pluginId 并先停用卸载，避免“删文件但类仍在内存”
        String pluginId = null;
        if (enabled.exists()) {
            pluginId = tryResolvePluginIdFromJar(enabled);
        } else if (disabled.exists()) {
            pluginId = tryResolvePluginIdFromJar(disabled);
        }
        if (pluginId != null) {
            stopPluginIfPossible(pluginId);
        }

        if (enabled.exists()) {
            Files.delete(enabled.toPath());
            exists = true;
        }
        if (disabled.exists()) {
            Files.delete(disabled.toPath());
            exists = true;
        }

        if (!exists) {
            return;
        }
    }

    /**
     * 根据插件 ID 删除其所有本地插件文件（启用/禁用），并尝试通过 PF4J 卸载
     * <p>
     * 该方法主要供集群客户端使用，当管理端下发“移除插件”指令时，
     * 可根据 pluginId 清理由本节点负责的所有本地副本。
     */
    public void deleteByPluginId(String pluginId) throws IOException {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("插件 ID 不能为空");
        }
        String normalizedId = pluginId.trim();

        // 1. 先尝试通过 PF4J 停用运行中的插件
        stopPluginIfPossible(normalizedId);

        // 2. 删除本地存储目录中该插件的所有 Jar 文件（启用/禁用）
        File rootDir = getRootDir();
        File[] files = rootDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            String name = file.getName();
            if (!name.endsWith(".jar") && !name.endsWith(".jar.disable")) {
                continue;
            }
            try {
                PluginJarMeta meta = resolveStandardJarName(file, name);
                if (normalizedId.equals(meta.getPluginId())) {
                    Files.delete(file.toPath());
                }
            } catch (IllegalArgumentException ex) {
                // 非合法插件包，忽略
                log.debug("按插件 ID 删除插件文件时解析失败, file={}, msg={}", file.getAbsolutePath(), ex.getMessage());
            }
        }
    }

    /**
     * 列出所有插件文件
     */
    public List<PluginInfo> listPlugins() {
        // 使用 fullPath 作为 key 聚合信息，避免重复
        java.util.Map<String, PluginInfo> map = new java.util.LinkedHashMap<>();

        // 1. 先从文件系统中扫描插件 Jar（无论启用/禁用），并尽量解析出 pluginId
        File rootDir = getRootDir();
        File[] files = rootDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                String name = file.getName();
                boolean enabled;
                if (name.endsWith(".jar.disable")) {
                    enabled = false;
                } else if (name.endsWith(".jar")) {
                    enabled = true;
                } else {
                    // 非插件文件，忽略
                    continue;
                }

                String pluginId = null;
                try {
                    PluginJarMeta meta = resolveStandardJarName(file, name);
                    pluginId = meta.getPluginId();
                } catch (IllegalArgumentException ex) {
                    // 非合法插件包，保留文件信息但 pluginId 为空，方便运维排查
                    log.warn("扫描插件文件时解析插件元数据失败, file={}, msg={}", file.getAbsolutePath(), ex.getMessage());
                }

                PluginInfo info = new PluginInfo()
                    .setPluginId(pluginId)
                    .setFileName(name)
                    .setEnabled(enabled)
                    .setFullPath(file.getAbsolutePath());
                map.put(file.getAbsolutePath(), info);
            }
        }

        // 2. 再叠加 PF4J 运行时信息（主要用于补全 pluginId 或纳入非默认目录的插件）
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            Path path = plugin.getPluginPath().toAbsolutePath().normalize();
            String fullPath = path.toString();
            PluginInfo info = map.get(fullPath);
            if (info == null) {
                boolean enabled = plugin.getPluginState() != PluginState.UNLOADED;
                info = new PluginInfo(plugin.getPluginId(), path.getFileName().toString(), enabled, fullPath);
                map.put(fullPath, info);
            } else {
                // 以运行时信息为准补全/覆盖 pluginId
                info.setPluginId(plugin.getPluginId());
            }
        }

        return new ArrayList<>(map.values());
    }

}
