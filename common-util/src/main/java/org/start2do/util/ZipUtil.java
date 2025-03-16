package org.start2do.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

@Slf4j
@UtilityClass
public class ZipUtil {

    /**
     * 解压 ZIP 文件，并支持指定 index.html 作为根目录
     *
     * @param zipFileInputStream ZIP 文件输入流
     * @param destPath           解压目标路径
     * @param rootFileName       指定的 index.html 路径（可为空）
     * @return 解压后的文件路径列表
     */
    public List<String> unzip(InputStream zipFileInputStream, Path destPath, String rootFileName) {
        List<String> result = new ArrayList<>();
        if (!Files.exists(destPath)) {
            try {
                Files.createDirectories(destPath);
            } catch (IOException e) {
                log.error("创建目录失败,{}", e.getMessage());
                return result;
            }
        }
        FileUtil.delete(destPath);
        // 首先将输入流转换为字节数组，以便多次读取
        byte[] zipBytes;
        try {
            zipBytes = IOUtils.toByteArray(zipFileInputStream);
            zipFileInputStream.close();
        } catch (IOException e) {
            log.error("读取ZIP文件失败,{}", e.getMessage());
            return result;
        }

        try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            // 查找 index.html 所在的根目录
            ZipArchiveInputStream inputStream = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes));
            String rootDir = findRootDirectory(inputStream, rootFileName);
            inputStream.close();
            // 解压文件
            ZipArchiveEntry entry;
            while ((entry = zipInputStream.getNextZipEntry()) != null) {
                // 如果指定了 index.html，只解压根目录下的文件
                String entryName = entry.getName();
                if (rootDir != null && !entryName.startsWith(rootDir) || entryName.substring(0, entryName.length() - 1)
                    .equals(rootDir)) {
                    continue;
                }

                Path entryPath = destPath.resolve(
                    entry.isDirectory() ? entryName : removeRootDir(entryName, rootDir));
                result.add(entryPath.toString());

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.copy(zipInputStream, entryPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            log.error("解压文件失败,{}", e.getMessage());
        }
        return result;
    }

    /**
     * 查找 index.html 所在的根目录
     *
     * @param zipInputStream ZIP 文件输入流
     * @param rootFileName   指定的 index.html 路径
     * @return 根目录路径（如果未找到则返回 null）
     * @throws IOException 如果读取 ZIP 文件失败
     */
    private String findRootDirectory(ZipArchiveInputStream zipInputStream, String rootFileName) throws IOException {
        if (rootFileName == null || rootFileName.isEmpty()) {
            return null;
        }

        ZipArchiveEntry entry;
        while ((entry = zipInputStream.getNextZipEntry()) != null) {
            if (entry.getName().equals(rootFileName)) {
                return "";
            } else if (entry.getName().endsWith("/" + rootFileName)) {
                return entry.getName().replace("/" + rootFileName, "");
            }
        }
        return null;
    }

    /**
     * 移除根目录前缀
     *
     * @param entryName 原始条目名称
     * @param rootDir   根目录路径
     * @return 移除根目录后的条目名称
     */
    private String removeRootDir(String entryName, String rootDir) {
        if (rootDir == null || rootDir.isEmpty()) {
            return entryName;
        }
        // 移除根目录前缀和斜杠
        return entryName.substring(rootDir.length() + 1);
    }

    public void addDirectoryToZip(File directory, String parentPath, ZipArchiveOutputStream zipOut) throws IOException {
        // 构造当前目录的路径
        String currentPath = parentPath + directory.getName() + "/";
        ZipArchiveEntry dirEntry = new ZipArchiveEntry(currentPath);
        zipOut.putArchiveEntry(dirEntry);
        zipOut.closeArchiveEntry();
        // 遍历目录中的文件和子目录
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归处理子目录
                    addDirectoryToZip(file, currentPath, zipOut);
                } else {
                    // 处理文件
                    addFileToZip(file, currentPath, zipOut);
                }
            }
        }
    }

    public void addFileToZip(File file, String parentPath, ZipArchiveOutputStream zipOut) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            // 创建文件条目
            ZipArchiveEntry fileEntry = new ZipArchiveEntry(parentPath + file.getName());
            zipOut.putArchiveEntry(fileEntry);
            // 写入文件内容
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.closeArchiveEntry();
        }
    }

}
