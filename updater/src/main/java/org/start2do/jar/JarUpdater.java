package org.start2do.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

public class JarUpdater {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java JarUpdater <jar路径> <jar中的相对路径> <更新zip包路径>");
            System.exit(1);
        }
        String jarPath = args[0];
        String relativePath = args[1];
        String zipPath = args[2];
        try {
            updateJarContent(jarPath, relativePath, zipPath);
            System.out.println("JAR更新成功！");
        } catch (Exception e) {
            System.err.println("更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateJarContent(String jarPath, String relativePath, String zipPath) throws IOException {
        // 创建临时JAR文件
        File tempJarFile = Files.createTempFile("temp", ".jar").toFile();
        tempJarFile.deleteOnExit();

        try (JarFile originalJar = new JarFile(jarPath); JarOutputStream tempJar = new JarOutputStream(
            new FileOutputStream(tempJarFile))) {

            // 复制原JAR内容，跳过需要更新的路径
            Enumeration<JarEntry> entries = originalJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(relativePath)) {
                    // 保留不需要更新的文件
                    tempJar.putNextEntry(new JarEntry(entry.getName()));
                    try (InputStream is = originalJar.getInputStream(entry)) {
                        IOUtils.copy(is, tempJar);
                    }
                }
            }

            // 添加新文件（使用 commons-compress 解压 ZIP）
            try (ZipFile zipFile = new ZipFile(new File(zipPath))) {
                for (Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries(); zipEntries.hasMoreElements(); ) {
                    ZipArchiveEntry zipEntry = zipEntries.nextElement();
                    String newEntryName = relativePath + "/" + zipEntry.getName();
                    if (!zipEntry.isDirectory()) {
                        tempJar.putNextEntry(new JarEntry(newEntryName));
                        try (InputStream is = zipFile.getInputStream(zipEntry)) {
                            IOUtils.copy(is, tempJar);
                        }
                    }
                }
            }
        }

        // 替换原始JAR文件
        Files.move(tempJarFile.toPath(), Paths.get(jarPath), StandardCopyOption.REPLACE_EXISTING);
    }
}
