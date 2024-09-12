package com.google.code.kaptcha.util;

import com.google.code.kaptcha.impl.DefaultKaptcha;
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

    public void init() {
        System.setProperty("java.awt.headless", "true");
        try {
            register("/imageCaptchaFont.ttf", "fonts/imageCaptchaFont.ttf");
            register("/SourceHanSansSC_Bold_Min.ttf", "fonts/SourceHanSansSC_Bold_Min.ttf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String registerFont(String basePath, String fontFile, InputStream inputStream) {
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
                } else {
                    throw new RuntimeException("inputStream is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fontPath;
    }

    public void register(String fileName, String path) throws IOException {
        InputStream inputStream = DefaultKaptcha.class.getClassLoader().getResourceAsStream(path);
        log.info("字体文件:{}:,{}", fileName, inputStream != null);
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT,
                Paths.get(registerFont(null, fileName, inputStream)).toFile());
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
