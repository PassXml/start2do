package org.start2do.plugin.manager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.pf4j.BasePluginLoader;
import org.pf4j.DefaultPluginClasspath;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginManager;

/**
 * 兼容 Spring Boot 可执行 JAR 解压后的目录布局。
 * <p>
 * PF4J 默认仅扫描 {@code classes/} 与 {@code lib/}，而 Spring Boot fat jar
 * 解压后类与依赖位于 {@code BOOT-INF/classes/} 和 {@code BOOT-INF/lib/}。
 */
public class SpringBootAwarePluginLoader extends BasePluginLoader {

    private static final String BOOT_CLASSES_DIR = "BOOT-INF/classes";
    private static final String BOOT_LIB_DIR = "BOOT-INF/lib";

    public SpringBootAwarePluginLoader(PluginManager pluginManager) {
        super(pluginManager, new DefaultPluginClasspath());
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return super.isApplicable(pluginPath) && Files.isDirectory(pluginPath);
    }

    @Override
    protected void loadClasses(Path pluginPath, PluginClassLoader pluginClassLoader) {
        super.loadClasses(pluginPath, pluginClassLoader);
        addClassesDirectory(pluginPath.resolve(BOOT_CLASSES_DIR), pluginClassLoader);
    }

    @Override
    protected void loadJars(Path pluginPath, PluginClassLoader pluginClassLoader) {
        super.loadJars(pluginPath, pluginClassLoader);
        addJarsDirectory(pluginPath.resolve(BOOT_LIB_DIR), pluginClassLoader);
    }

    private void addClassesDirectory(Path path, PluginClassLoader pluginClassLoader) {
        File directory = path.toFile();
        if (directory.exists() && directory.isDirectory()) {
            pluginClassLoader.addFile(directory);
        }
    }

    private void addJarsDirectory(Path path, PluginClassLoader pluginClassLoader) {
        File[] jars = path.toFile().listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null) {
            return;
        }
        for (File jar : jars) {
            if (jar.isFile()) {
                pluginClassLoader.addFile(jar);
            }
        }
    }
}
