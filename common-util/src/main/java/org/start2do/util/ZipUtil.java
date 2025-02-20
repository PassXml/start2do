package org.start2do.util;

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

@Slf4j
@UtilityClass
public class ZipUtil {

    public List<String> unzip(InputStream zipFileInputStream, Path destPath) {
        List<String> result = new ArrayList<>();
        if (!Files.exists(destPath)) {
            try {
                Files.createDirectories(destPath);
            } catch (IOException e) {
                log.error("创建目录失败,{}", e.getMessage());
            }
        }
        try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(zipFileInputStream)) {
            ZipArchiveEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = destPath.resolve(entry.getName());
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
}
