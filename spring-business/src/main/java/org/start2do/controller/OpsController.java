package org.start2do.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.dto.R;
import org.start2do.util.FileUtil;
import org.start2do.util.ZipUtil;

@RestController
@RequestMapping("/ops")
@Slf4j
@RequiredArgsConstructor
public class OpsController {

    private final List<String> whitePath = List.of("/tmp/safezone", "/opt/deploy");

    @PostMapping("/deploy")
    public ResponseEntity<R<String>> deploy(@RequestParam("destPath") String destPath,
        @RequestParam("file") MultipartFile multipartFile, @RequestParam("clear") String clearStr,
        @RequestParam("rootFileName") String rootFileName,
        @RequestParam(value = "unzip", defaultValue = "true") String unzipStr) {

        if (!FileUtil.isPathAllowed(this.whitePath, destPath)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(R.failed(HttpStatus.FORBIDDEN.value(), String.format("%s 不在白名单内", destPath)));
        }

        try {
            Path destinationPath = Paths.get(destPath);
            Path parentDir = destinationPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            } else if (!Files.exists(destinationPath)) {
                Files.createDirectories(destinationPath);
            }

            boolean clear = Boolean.parseBoolean(clearStr);
            if (clear) {
                FileUtil.cleanTargetFolder(destPath);
            }

            if (multipartFile.isEmpty()) {
                return ResponseEntity.badRequest().body(R.failed("上传文件不能为空"));
            }

            boolean isZip = false;
            try (InputStream inputStream = multipartFile.getInputStream()) {
                if (multipartFile.getContentType() != null && (multipartFile.getContentType().equals("application/zip")
                                                               || multipartFile.getContentType()
                                                                   .equals("application/x-zip-compressed"))) {
                    isZip = true;
                } else {
                    byte[] header = new byte[4];
                    try (InputStream headerIs = multipartFile.getInputStream()) {
                        BufferedInputStream bis = new BufferedInputStream(headerIs);
                        bis.mark(4);
                        int bytesRead = bis.read(header);
                        bis.reset();
                        if (bytesRead == 4 && header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03
                            && header[3] == 0x04) {
                            isZip = true;
                        }
                    }
                }
            }

            boolean unzip = Boolean.parseBoolean(unzipStr);

            if (isZip && unzip) {
                try (InputStream inputStream = multipartFile.getInputStream()) {
                    ZipUtil.unzipStream(inputStream, destinationPath, rootFileName);
                }
            } else {
                String originalFilename = multipartFile.getOriginalFilename();
                if (originalFilename == null || originalFilename.contains("..")) {
                    return ResponseEntity.badRequest().body(R.failed("无效的文件名"));
                }
                Path targetFilePath = Paths.get(destPath).resolve(originalFilename).normalize();

                if (!targetFilePath.startsWith(Paths.get(destPath).normalize())) {
                    return ResponseEntity.badRequest().body(R.failed("无效的文件路径"));
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
                .body(R.failed("部署失败: " + e.getMessage()));
        }
    }

    @GetMapping("/download")
    public void downloadFiles(@RequestParam("path") List<String> paths, HttpServletResponse response) {
        if (paths == null || paths.isEmpty()) {
            try {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter()
                    .write(new R<>().setCode(HttpStatus.BAD_REQUEST.value()).setMsg("未提供路径").toJson());
            } catch (IOException e) {
                log.error("Error writing error response for downloadFiles: {}", e.getMessage());
            }
            return;
        }

        for (String path : paths) {
            if (!FileUtil.isPathAllowed(this.whitePath, path)) {
                try {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write(new R<>().setCode(HttpStatus.FORBIDDEN.value())
                        .setMsg(String.format("路径访问不允许: %s", path)).toJson());
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
                    ZipUtil.addDirectoryToZipNIO(zos, pathStr, baseInZip);

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
                    ZipUtil.addFileToZipNIO(zos, pathStr, entryNameInZip);
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
