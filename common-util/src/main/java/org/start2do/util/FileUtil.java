package org.start2do.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
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
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
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
            Files.write(path, body.getBytes(StandardCharsets.UTF_8), options);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public List<String> readAllLine(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取文件名,不带后缀
     */
    public String getFileName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        }
        int lasted = Math.max(name.lastIndexOf("/"), name.lastIndexOf("\\"));
        if (lasted == -1) {
            return name.substring(0, i);
        } else {
            return name.substring(lasted + 1, i);
        }
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
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
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

            FileOutputStream outputStream = new FileOutputStream(Paths.get(filePath).toFile());
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String path) {
        delete(Paths.get(path));
    }

    public void delete(Path destPath) {
        try {
            Files.walkFileTree(destPath, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(destPath)) {
                        Files.deleteIfExists(dir);
                    }
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

    public static String getLastDirname(Path parent, int i) {
        if (parent != null && i >= 0) {
            String path = parent.toString();
            String[] parts = path.split("\\".equals(File.separator) ? "\\\\" : File.separator);
            if (parts.length > 0) {
                return parts[parts.length - 1 - i];
            }
        }
        return null;
    }

    public static boolean isPathAllowed(List<String> whitePath, String pathString) {
        if (whitePath == null || whitePath.isEmpty()) {
            return false;
        }
        Path path = Paths.get(pathString).normalize();
        for (String allowedPrefix : whitePath) {
            if (path.startsWith(Paths.get(allowedPrefix).normalize())) {
                return true;
            }
        }
        return false;
    }

    public static void cleanTargetFolder(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        if (Files.exists(path) && Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(path)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else if (Files.exists(path) && !Files.isDirectory(path)) {
            Files.delete(path);
        }
        Files.createDirectories(path);
    }

    /**
     * 新的后缀
     */
    public String newSuffix(String dest, String newSuffix) {
        if (dest == null || newSuffix == null) {
            return null;
        }
        return dest.substring(0, dest.lastIndexOf(".")) + "." + newSuffix;
    }

    public static String getRelativeFilePath(Path basePath, Path path) {
        String string = basePath.relativize(path).toString();
        return string.replaceAll("\\\\", "/");
    }

    @Getter
    private static final Map<String, Thread> THREAD_MAP = new ConcurrentHashMap<>();

    public static void watch(String dirPath, Consumer<WatchEvent<?>> callback) {
        watch(Paths.get(dirPath), callback);
    }

    /**
     * 监听文件目录,如果文件变更,则调用Callback
     */
    public static void watch(Path dir, Consumer<WatchEvent<?>> callback) {
        String path = dir.toAbsolutePath().toString();
        log.info("开始监听文件夹,{}", path);
        Thread thread = THREAD_MAP.get(path);
        if (thread != null) {
            log.info("存在重复监听任务,{},停止之前的监听", path);
            thread.interrupt(); // 使用 interrupt 代替 stop
        }
        if (!Files.exists(dir)) {
            log.info("脚本文件目录不存在,不监听了");
            return;
        }
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) { // 检查中断状态
                    try {
                        WatchKey key;
                        try {
                            key = watcher.take();
                        } catch (InterruptedException x) {
                            Thread.currentThread().interrupt(); // 重新设置中断状态
                            break; // 退出循环
                        }
                        for (WatchEvent<?> event : key.pollEvents()) {
                            callback.accept(event);
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    } catch (ClosedWatchServiceException x) {
                        break;
                    }
                }
            });
            thread.start();
            THREAD_MAP.put(path, thread);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件夹下的所有文件
     *
     * @param targetPath  目标路径
     * @param excludePath 排除路径
     * @return 文件列表
     */
    public List<Path> walk(Path targetPath, String... excludePath) {
        List<Path> collect = new LinkedList<>();
        try {
            if (!targetPath.toFile().exists()) {
                log.warn("文件不存在");
                return collect;
            }
            collect.addAll(Files.walk(targetPath).filter(p -> {
                if (p.equals(targetPath)) {
                    return false;
                }
                for (String string : excludePath) {
                    Path resolve = targetPath.resolve(string);
                    String all = resolve.relativize(p).toString();
                    if (resolve.equals(p) || !all.startsWith(".")) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return collect;
    }

    public List<DeleteResult> delete(Path destPath, String... excludePath) {
        List<Path> paths = walk(destPath, excludePath);
        List<Path> dirPath = new LinkedList<>();
        List<DeleteResult> results = new ArrayList<>();
        for (Path p : paths) {
            try {
                if (!Files.isDirectory(p)) {
                    Files.deleteIfExists(p);
                    results.add(new DeleteResult(
                        true, p
                    ));
                } else {
                    dirPath.add(p);
                }

            } catch (IOException e) {
                log.error("删除文件失败,{}", e.getMessage());
                results.add(new DeleteResult(
                    false, p
                ));
            }
        }
        for (Path dir : dirPath) {
            try {
                Files.deleteIfExists(dir);
                results.add(new DeleteResult(
                    true, dir
                ));
            } catch (IOException e) {
                log.error("删除文件目录失败,{}", e.getMessage());
                results.add(new DeleteResult(
                    false, dir
                ));
            }
        }
        return results;
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    @ToString
    public static class DeleteResult {

        private boolean success;
        private Path path;

        public DeleteResult(boolean success, Path path) {
            this.success = success;
            this.path = path;
        }
    }
}
