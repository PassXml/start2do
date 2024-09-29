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
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FileUtil {

    public String getJavaTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public List<String> readAllLine(String filePath) {
        Path path = Paths.get(filePath);
        File file = path.toFile();
        if (!file.exists()) {
            return List.of();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    public boolean writeString(String filePath, String body, StandardOpenOption... options) {
        return writeString(Paths.get(filePath), body, options);
    }

    public boolean writeString(Path path, String body, StandardOpenOption... options) {
        try {
            Path parent = path.getParent();
            if (!Files.exists(parent)) {
                parent.toFile().mkdirs();
            }
            Files.writeString(path, body, options);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public List<String> readAllLine(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            return List.of();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    public String getFileName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        }
        return name.substring(0, i);
    }

    public String getSuffix(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        }
        return name.substring(i + 1);
    }

    public String readString(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            return "";
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public List<File> walkFilterByFile(File file, Predicate<File> predicate) {
        List<File> result = new ArrayList<>();
        if (file.isDirectory()) {//如果是目录
            File[] listFiles = file.listFiles();//获取当前路径下的所有文件和目录,返回File对象数组
            for (File f : listFiles) {//将目录内的内容对象化并遍历
                result.addAll(walkFilterByFile(f, predicate));

            }
        } else if (file.isFile()) {//如果是文件
            if (predicate.test(file)) {
                result.add(file);
            }
        }
        return result;
    }

    public List<String> walk(File file, Predicate<String> fileNameFilter) {
        List<String> result = new ArrayList<>();
        if (file.isDirectory()) {//如果是目录
            File[] listFiles = file.listFiles();//获取当前路径下的所有文件和目录,返回File对象数组
            for (File f : listFiles) {//将目录内的内容对象化并遍历
                result.addAll(walk(f, fileNameFilter));

            }
        } else if (file.isFile()) {//如果是文件
            if (fileNameFilter.test(file.getName())) {
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

    public static String getDirname(Path parent, int i) {
        if (parent == null || i < 0) {
            return null;
        }
        String path = parent.toString();
        String[] parts = path.split("\\".equals(File.separator) ? "\\\\" : File.separator);
        if (i >= parts.length) {
            return null;
        }
        return parts[parts.length - 1 - i];
    }

    /**
     * 输入路径信息以及想要的层级,返回对应的路径信息
     */
    public static String getDirPath(Path parent, int level) {
        if (parent == null || level < 0) {
            return null;
        }
        String path = parent.toString();
        String[] parts = path.split("\\".equals(File.separator) ? "\\\\" : File.separator);
        if (level >= parts.length) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < level; i++) {
            result.append(parts[i]).append(File.separator);
        }
        return result.toString();
    }
}
