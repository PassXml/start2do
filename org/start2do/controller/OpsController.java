package org.start2do.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.dto.R;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/ops")
@Slf4j
@RequiredArgsConstructor
public class OpsController {

    private final List<String> whitePath = List.of("/tmp/safezone", "/opt/deploy");

    @PostMapping("/deploy")
    public ResponseEntity<R<String>> deploy(
            @RequestParam("destPath") String destPath,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("clear") String clearStr,
            @RequestParam("rootFileName") String rootFileName,
            @RequestParam(value = "unzip", defaultValue = "true") String unzipStr) {

        if (!FileUtils.isPathAllowed(this.whitePath, destPath)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(R.fail(HttpStatus.FORBIDDEN.value(), String.format("%s 不在白名单内", destPath)));
        }

        try {
            Path destinationPath = Paths.get(destPath);
            Path parentDir = destinationPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            } else if (!Files.exists(destinationPath)){
                Files.createDirectories(destinationPath);
            }

            boolean clear = Boolean.parseBoolean(clearStr);
            if (clear) {
                FileUtils.cleanTargetFolder(destPath);
            }

            if (multipartFile.isEmpty()) {
                return ResponseEntity.badRequest().body(R.error("上传文件不能为空"));
            }

            boolean isZip = false;
            try (InputStream inputStream = multipartFile.getInputStream()) {
                if (multipartFile.getContentType() != null && 
                    (multipartFile.getContentType().equals("application/zip") || multipartFile.getContentType().equals("application/x-zip-compressed"))) {
                    isZip = true;
                } else {
                    byte[] header = new byte[4];
                    try (InputStream headerIs = multipartFile.getInputStream()) {
                         BufferedInputStream bis = new BufferedInputStream(headerIs);
                         bis.mark(4);
                         int bytesRead = bis.read(header);
                         bis.reset();
                         if (bytesRead == 4 && header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04) {
                            isZip = true;
                         }
                    }
                }
            }

            boolean unzip = Boolean.parseBoolean(unzipStr);

            if (isZip && unzip) {
                try (InputStream inputStream = multipartFile.getInputStream()) {
                    FileUtils.unzipStream(inputStream, destPath, rootFileName);
                }
            } else {
                String originalFilename = multipartFile.getOriginalFilename();
                if (originalFilename == null || originalFilename.contains("..")) {
                     return ResponseEntity.badRequest().body(R.error("无效的文件名"));
                }
                Path targetFilePath = Paths.get(destPath).resolve(originalFilename).normalize();
                
                if (!targetFilePath.startsWith(Paths.get(destPath).normalize())) {
                    return ResponseEntity.badRequest().body(R.error("无效的文件路径"));
                }

                Files.createDirectories(targetFilePath.getParent());
                try (InputStream inputStream = multipartFile.getInputStream()) {
                    Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            return ResponseEntity.ok(R.ok("部署成功"));

        } catch (IOException e) {
            log.error("部署失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(R.error("部署失败: " + e.getMessage()));
        }
    }

    @GetMapping("/download")
    public void downloadFiles(@RequestParam("path") List<String> paths, HttpServletResponse response) {
        if (paths == null || paths.isEmpty()) {
            try {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter().write(new R<>().setCode(HttpStatus.BAD_REQUEST.value()).setMsg("未提供路径").toJson());
            } catch (IOException e) {
                log.error("Error writing error response for downloadFiles: {}", e.getMessage());
            }
            return;
        }

        for (String path : paths) {
            if (!FileUtils.isPathAllowed(this.whitePath, path)) {
                 try {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write(new R<>().setCode(HttpStatus.FORBIDDEN.value()).setMsg(String.format("路径访问不允许: %s", path)).toJson());
                } catch (IOException e) {
                    log.error("Error writing error response for downloadFiles: {}", e.getMessage());
                }
                return;
            }
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"files.zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (String pathStr : paths) {
                Path path = Paths.get(pathStr);
                if (!Files.exists(path)) {
                    log.warn("路径未找到，跳过: {}", pathStr);
                    continue;
                }

                if (Files.isDirectory(path)) {
                    String baseInZip = "";
                    if (paths.size() > 1 || Files.isDirectory(path)) {
                         baseInZip = path.getFileName().toString() + "/";
                    }
                    FileUtils.addDirectoryToZip(zos, pathStr, baseInZip);

                } else {
                    String entryNameInZip = "";
                    if (paths.size() == 1) {
                        entryNameInZip = path.getFileName().toString();
                    } else {
                        Path parent = path.getParent();
                        if (parent != null) {
                             entryNameInZip = parent.getFileName().toString() + "/" + path.getFileName().toString();
                        } else {
                             entryNameInZip = path.getFileName().toString();
                        }
                    }
                    FileUtils.addFileToZip(zos, pathStr, entryNameInZip);
                }
            }
            zos.finish();
        } catch (IOException e) {
            log.error("下载文件并打包为 ZIP 失败: {}", e.getMessage(), e);
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }
}

class FileUtils {
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

    public static void unzipStream(InputStream inputStream, String destPathString, String rootFileName) throws IOException {
        Path destPath = Paths.get(destPathString).normalize();
        if (!Files.exists(destPath)) {
            Files.createDirectories(destPath);
        }

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream))) {
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            while ((zipEntry = zis.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (rootFileName != null && !rootFileName.isEmpty() && entryName.startsWith(rootFileName + "/")) {
                    entryName = entryName.substring(rootFileName.length() + 1);
                }
                if (entryName.isEmpty()) continue;

                Path newFile = destPath.resolve(entryName).normalize();

                if (!newFile.startsWith(destPath)) {
                    throw new IOException("Zip entry is outside of the target dir: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    try (OutputStream fos = Files.newOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public static void addDirectoryToZip(ZipOutputStream zos, String dirPathString, String baseInZip) throws IOException {
        Path dirPath = Paths.get(dirPathString);
        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String entryName = baseInZip + dirPath.relativize(dir).toString().replace("\\", "/") + "/";
                if (!entryName.equals("/")) {
                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.closeEntry();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String entryName = baseInZip + dirPath.relativize(file).toString().replace("\\", "/");
                zos.putNextEntry(new ZipEntry(entryName));
                Files.copy(file, zos);
                zos.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    public static void addFileToZip(ZipOutputStream zos, String filePathString, String entryNameInZip) throws IOException {
        Path filePath = Paths.get(filePathString);
        if (entryNameInZip == null || entryNameInZip.isEmpty() || entryNameInZip.equals("/")) {
            entryNameInZip = filePath.getFileName().toString();
        }
        if (entryNameInZip.startsWith("/")) {
            entryNameInZip = entryNameInZip.substring(1);
        }

        ZipEntry zipEntry = new ZipEntry(entryNameInZip);
        zos.putNextEntry(zipEntry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }
}
