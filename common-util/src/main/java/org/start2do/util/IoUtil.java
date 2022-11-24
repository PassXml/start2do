package org.start2do.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IoUtil {

    public void exportTmp(String fileName, InputStream inputStream) {
        String basePath = System.getProperty("java.io.tmpdir");
        export(basePath, fileName, inputStream);
    }

    @SneakyThrows
    public void export(String basePath, String fileName, InputStream inputStream) {
        Path path = Paths.get(basePath + fileName);
        if (Files.exists(path)) {
            return;
        }
        Files.copy(
            inputStream, path
        );
    }
}
