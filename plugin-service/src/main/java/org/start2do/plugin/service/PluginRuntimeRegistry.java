package org.start2do.plugin.service;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * 插件运行态辅助注册表（宿主侧）。
 * <p>
 * 用于记录插件包文件指纹，并检测“同路径 JAR 被外部覆盖但插件仍在运行”的场景，
 * 从而触发必要的卸载/重载，避免出现“必须重启宿主才会读到新配置/新资源”的错觉。
 */
@Component
public class PluginRuntimeRegistry {

    private final ConcurrentMap<String, PluginJarFingerprint> jarFingerprintByPluginId = new ConcurrentHashMap<>();

    public void recordJarFingerprint(String pluginId, File jarFile) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return;
        }
        PluginJarFingerprint fp = PluginJarFingerprint.fromFile(jarFile);
        if (fp == null) {
            return;
        }
        jarFingerprintByPluginId.put(pluginId.trim(), fp);
    }

    public void recordJarFingerprint(String pluginId, Path jarPath) {
        if (jarPath == null) {
            return;
        }
        recordJarFingerprint(pluginId, jarPath.toFile());
    }

    /**
     * 是否检测到“文件指纹发生变化”。
     * <p>
     * 注意：若此前未记录过指纹，返回 false（并不会强制判定为变化）。
     */
    public boolean hasJarFingerprintChanged(String pluginId, File jarFile) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return false;
        }
        PluginJarFingerprint last = jarFingerprintByPluginId.get(pluginId.trim());
        if (last == null) {
            return false;
        }
        PluginJarFingerprint cur = PluginJarFingerprint.fromFile(jarFile);
        return cur != null && !last.equals(cur);
    }

    public void clear(String pluginId) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            return;
        }
        jarFingerprintByPluginId.remove(pluginId.trim());
    }

    static final class PluginJarFingerprint {
        private final long lastModified;
        private final long length;

        private PluginJarFingerprint(long lastModified, long length) {
            this.lastModified = lastModified;
            this.length = length;
        }

        private static PluginJarFingerprint fromFile(File f) {
            if (f == null || !f.isFile()) {
                return null;
            }
            return new PluginJarFingerprint(f.lastModified(), f.length());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PluginJarFingerprint)) {
                return false;
            }
            PluginJarFingerprint that = (PluginJarFingerprint) o;
            return lastModified == that.lastModified && length == that.length;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lastModified, length);
        }

        @Override
        public String toString() {
            return "fingerprint{lastModified=" + lastModified + ", length=" + length + "}";
        }
    }
}

