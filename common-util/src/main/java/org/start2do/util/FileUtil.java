package org.start2do.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FileUtil {

    public List<String> getFiles(File file, Predicate<String> predicate) {
        List<String> result = new ArrayList<>();
        if (file.isDirectory()) {//如果是目录
            File[] listFiles = file.listFiles();//获取当前路径下的所有文件和目录,返回File对象数组
            for (File f : listFiles) {//将目录内的内容对象化并遍历
                result.addAll(getFiles(f, predicate));

            }
        } else if (file.isFile()) {//如果是文件
            if (predicate.test(file.getName())) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

    public void writeFile(String filePath, InputStream inputStream) {
        try {
            inputStream.transferTo(new FileOutputStream(Paths.get(filePath).toFile()));
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String path) {
        try {
            Files.walkFileTree(Paths.get(path), new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
                    path.toFile().delete();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                    path.toFile().delete();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
