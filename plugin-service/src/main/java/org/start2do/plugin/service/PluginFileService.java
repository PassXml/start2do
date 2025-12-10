package org.start2do.plugin.service;

import java.util.Optional;
import org.start2do.plugin.config.PluginStorageProperties;
import org.start2do.plugin.dto.PluginInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 插件文件管理服务
 * <p>
 * 约定： - 启用：{name}.jar - 禁用：{name}.jar.disable
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginFileService {

    private final PluginStorageProperties properties;

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
        String path = properties.getStoragePath();
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
        try {
            if (isRunStateByFilePath(enabledJar.toPath())) {
                throw new RuntimeException("插件已运行");
            }
            // 若插件尚未加载，则按文件路径动态加载
            String pluginId = pluginManager.loadPlugin(enabledJar.toPath());
            pluginManager.startPlugin(pluginId);
            log.info("PF4J 动态加载并启动插件成功, baseName={}, pluginId={}", enabledJar.getAbsolutePath(), pluginId);
            return Optional.ofNullable(getPluginInfoById(pluginId)).map(PluginWrapper::getPluginState)
                .map(PluginState::toString).orElseGet(() -> PluginState.UNLOADED.toString());
        } catch (Exception e) {
            String name = enabledJar.toString();
            disabledFile(name);
            log.warn("PF4J 启用插件失败, baseName={}, msg={}", name, e.getMessage(), e);
        }
        return PluginState.UNLOADED.toString();
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

        validateName(originalName);

        File target = enable ? enabledFile(originalName) : disabledFile(originalName);
        if (target.exists()) {
            if (isRunStateByFilePath(target.toPath())) {
                PluginWrapper wrapper = getPluginInfoByFilePath(target);
                if (wrapper != null) {
                    stopPluginIfPossible(wrapper.getPluginId());
                }
            }
            target.delete();
        }
        getRootDir();
        file.transferTo(target);
        // 若上传后立即启用，则通知 PF4J 动态加载插件
        if (enable) {
            return startPluginIfPossible(target);
        }
        return null;
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
     * 启用插件：{name}.jar.disable -> {name}.jar
     */
    public String enable(String baseName) throws IOException {
        validateName(baseName);

        File disabled = disabledFile(baseName);
        File enabled = enabledFile(baseName);

        if (!disabled.exists()) {
            if (enabled.exists()) {
                // 文件已经是启用状态，尝试确保 PF4J 中也已启动
                return startPluginIfPossible(enabled);
            }
            throw new IllegalStateException("未找到禁用状态的插件文件: " + baseName);
        }

        if (enabled.exists()) {
            throw new IllegalStateException("启用目标文件已存在，请检查是否重名: " + enabled.getName());
        }

        Files.move(disabled.toPath(), enabled.toPath(), StandardCopyOption.ATOMIC_MOVE);

        // 文件层面启用后，同步到 PF4J 运行时
        return startPluginIfPossible(enabled);
    }

    /**
     * 停用插件：{name}.jar -> {name}.jar.disable
     */
    public void disable(String pluginId) throws IOException {
        String fileName = stopPluginIfPossible(pluginId);
        if (fileName != null) {
            Files.move(enabledFile(fileName).toPath(), disabledFile(fileName).toPath(), StandardCopyOption.ATOMIC_MOVE);
        }
    }

    /**
     * 删除插件：同时删除启用/禁用文件
     */
    public void delete(String jarFileName) throws IOException {
        validateName(jarFileName);

        File enabled = enabledFile(jarFileName);
        File disabled = disabledFile(jarFileName);

        boolean exists = false;

        if (enabled.exists()) {
            // 删除前先停用运行时插件
            stopPluginIfPossible(jarFileName);
            Files.delete(enabled.toPath());
            exists = true;
        }
        if (disabled.exists()) {
            ;
            exists = true;
        }

        if (!exists) {
            return;
        }
    }

    /**
     * 列出所有插件文件
     */
    public List<PluginInfo> listPlugins() {
        List<PluginInfo> result = new ArrayList<>();
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            Path path = plugin.getPluginPath();
            result.add(new PluginInfo(plugin.getPluginId(), path.getFileName().toString(),
                plugin.getPluginState() != PluginState.UNLOADED, path.toAbsolutePath().toString()));
        }
        File rootDir = getRootDir();
        File[] files = rootDir.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            String name = file.getName();
            boolean enabled = false;
            PluginInfo info = new PluginInfo().setPluginId(null).setFileName(name).setEnabled(enabled)
                .setFullPath(file.getAbsolutePath());
            result.add(info);
        }
        return result;
    }
}
