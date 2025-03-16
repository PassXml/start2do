package org.start2do.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IOUtil {

    public void exportTmp(String fileName, InputStream inputStream) {
        String basePath = System.getProperty("java.io.tmpdir");
        export(basePath, fileName, inputStream);
    }

    public void export(String basePath, String fileName, InputStream inputStream) {
        Path path = Paths.get(basePath + fileName);
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.copy(
                inputStream, path
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将输入流转换为字节数组
     *
     * @param inputStream 输入流
     * @return 字节数组
     * @throws IOException 如果读取过程中发生IO异常
     */
    public byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }
}
