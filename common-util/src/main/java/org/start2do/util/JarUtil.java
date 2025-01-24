package org.start2do.util;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JarUtil {

    private final Map<String, ClassLoader> jarClassLoaders = new HashMap<>();

    /**
     * 加载JAR文件中的类到JVM内存
     *
     * @param jarPath   JAR文件路径
     * @param className 要加载的类名
     * @return 加载的Class对象
     * @throws Exception 如果加载失败
     */
    public Class<?> loadClassFromJar(ClassLoader classLoader, String jarPath, String className) throws Exception {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new IOException("JAR file not found: " + jarPath);
        }
        jarClassLoaders.put(jarPath, classLoader);
        return classLoader.loadClass(className);
    }

    /**
     * 卸载JAR文件中的类并触发垃圾回收
     *
     * @param jarPath JAR文件路径
     * @throws Exception 如果卸载失败
     */
    public void unloadJar(String jarPath) throws Exception {
        ClassLoader classLoader = jarClassLoaders.remove(jarPath);
        if (classLoader == null) {
            throw new IOException("JAR file not loaded: " + jarPath);
        }
        if (classLoader instanceof URLClassLoader) {
            ((URLClassLoader) classLoader).close();
        }
        // 触发垃圾回收
        System.gc();
    }

    /**
     * 加载 JAR 文件中的所有类到 JVM 内存
     *
     * @param jarPath JAR 文件路径
     * @return 加载的类集合（类名 -> Class 对象）
     * @throws IOException            如果 JAR 文件读取失败
     * @throws ClassNotFoundException 如果类加载失败
     */
    public Map<String, Class<?>> loadAllClassesFromJar(ClassLoader classLoader, String jarPath)
        throws IOException, ClassNotFoundException {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new IOException("JAR file not found: " + jarPath);
        }
        jarClassLoaders.put(jarPath, classLoader);
        Map<String, Class<?>> loadedClasses = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    // 将路径转换为类名（例如 com/example/MyClass.class -> com.example.MyClass）
                    String className = entry.getName()
                        .replace("/", ".")
                        .replace(".class", "");
                    // 加载类
                    Class<?> clazz = classLoader.loadClass(className);
                    loadedClasses.put(className, clazz);
                }
            }
        }

        return loadedClasses;
    }
}
