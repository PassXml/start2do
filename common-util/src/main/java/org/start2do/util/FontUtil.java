package org.start2do.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FontUtil {

    public File getBaseFile(String basePath, String fontFile) {
        if (basePath == null || basePath.isEmpty()) {
            basePath = System.getProperty("java.io.tmpdir");
        }
        String fontPath = basePath + fontFile;
        File file = Paths.get(fontPath).toFile();
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public void registerFont(File file) {
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, file);
            // 获取 GraphicsEnvironment 实例
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            // 注册字体
            boolean font = ge.registerFont(customFont);
            log.info("字体注册结果:{}", font);
            for (Font allFont : ge.getAllFonts()) {
                log.info("所有字体:{}", allFont.getFontName());
            }
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File exportFont(String basePath, String fontFile, InputStream inputStream) {
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
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
