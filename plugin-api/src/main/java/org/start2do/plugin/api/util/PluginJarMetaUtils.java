package org.start2do.plugin.api.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import lombok.experimental.UtilityClass;
import org.start2do.plugin.api.dto.PluginJarMeta;

/**
 * 插件 Jar 元数据解析工具。
 * <p>
 * 优先从 plugin.properties（PF4J {@code PropertiesPluginDescriptorFinder} 使用的文件）
 * 中读取 plugin.id / plugin.version，若不存在则回退到 Manifest 中的
 * Plugin-Id / Plugin-Version，确保与 PF4J 读取逻辑保持一致。
 */
@UtilityClass
public class PluginJarMetaUtils {

    /**
     * 从给定插件 Jar 中解析插件元数据。
     *
     * @param jarFile      插件 Jar 文件
     * @param originalName 原始文件名（仅用于错误提示）
     * @return 解析到的插件元数据
     * @throws IllegalArgumentException 当无法从 plugin.properties 或 Manifest 中解析到合法元数据时抛出
     */
    public PluginJarMeta resolveFromJar(File jarFile, String originalName) {
        if (jarFile == null || !jarFile.isFile()) {
            throw new IllegalArgumentException("插件 Jar 文件不存在或不是普通文件: " + originalName);
        }

        try (JarFile jar = new JarFile(jarFile)) {
            // 1. 优先从 plugin.properties 中解析（与 PropertiesOnlyJarPluginManager 保持一致）
            PluginJarMeta fromProps = resolveFromPluginProperties(jar);
            if (fromProps != null) {
                return fromProps;
            }

            // 2. 回退到 Manifest 中的 Plugin-Id / Plugin-Version
            PluginJarMeta fromManifest = resolveFromManifest(jar);
            if (fromManifest != null) {
                return fromManifest;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                "解析插件 Jar 元数据失败，请检查是否包含有效的 plugin.properties 或 Manifest: " + originalName, e);
        }

        throw new IllegalArgumentException(
            "插件 Jar 中未找到 plugin.id / plugin.version 或 Manifest 中的 Plugin-Id / Plugin-Version，请检查构建配置: "
                + originalName);
    }

    /**
     * 从 plugin.properties 中解析插件元数据。
     */
    private PluginJarMeta resolveFromPluginProperties(JarFile jar) throws IOException {
        // 默认由注解处理器生成到 Jar 根目录；同时兼容 META-INF 路径
        JarEntry entry = jar.getJarEntry("plugin.properties");
        if (entry == null) {
            entry = jar.getJarEntry("META-INF/plugin.properties");
        }
        if (entry == null) {
            return null;
        }

        Properties props = new Properties();
        try (InputStream is = jar.getInputStream(entry)) {
            props.load(is);
        }

        String pluginId = trimToNull(props.getProperty("plugin.id"));
        String version = trimToNull(props.getProperty("plugin.version"));
        if (pluginId == null || version == null) {
            return null;
        }
        return new PluginJarMeta(pluginId, version);
    }

    /**
     * 从 Manifest 中解析插件元数据（兼容旧实现）。
     */
    private PluginJarMeta resolveFromManifest(JarFile jar) throws IOException {
        Manifest manifest = jar.getManifest();
        if (manifest == null) {
            return null;
        }
        Attributes attrs = manifest.getMainAttributes();
        String pluginId = trimToNull(attrs.getValue("Plugin-Id"));
        String version = trimToNull(attrs.getValue("Plugin-Version"));

        if (pluginId == null || version == null) {
            return null;
        }
        return new PluginJarMeta(pluginId, version);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

