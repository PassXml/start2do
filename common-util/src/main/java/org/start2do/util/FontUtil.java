package org.start2do.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FontUtil {

    public void registerFont(String basePath, String fontFile, InputStream inputStream) {
        if (basePath == null || basePath.length() < 1) {
            basePath = System.getProperty("java.io.tmpdir");
        }
        String fontPath = basePath + fontFile;
        File file = Paths.get(fontPath).toFile();
        if (!file.exists() && file.length() < 1) {
            try {
                if (inputStream != null) {
                    FileOutputStream outputStream = new FileOutputStream(fontPath);
                    byte[] buffer = new byte[1024];
                    int n;
                    while (-1 != (n = inputStream.read(buffer))) {
                        outputStream.write(buffer, 0, n);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
