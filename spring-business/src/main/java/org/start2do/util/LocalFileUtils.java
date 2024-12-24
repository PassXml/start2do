package org.start2do.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;
import org.start2do.dto.dto.file.FileUpdateResultDto;

@UtilityClass
public class LocalFileUtils {

    public FileUpdateResultDto upload(String uploadDir, String md5, String fileName, ByteArrayInputStream inputStream) {
        String finalMd5;
        if (md5 == null) {
            finalMd5 = Md5Util.md5(inputStream);
        } else {
            finalMd5 = md5;
        }
        String suffix = FileUtil.getSuffix(fileName);
        Path path = Paths.get(
            uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd") + File.separator
            + finalMd5 + "." + suffix);
        try {
            Files.createDirectories(path.getParent());
            byte[] bytes = inputStream.readAllBytes();
            File file = path.toFile();
            //文件不存在
            if (!file.exists()) {
                Files.write(path, bytes);
            }
            String relativeFilePath = FileUtil.getRelativeFilePath(Paths.get(uploadDir),
                path);
            return new FileUpdateResultDto(
                fileName, path.toString(), suffix, relativeFilePath, md5, bytes.length
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
