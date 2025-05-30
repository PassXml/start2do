package org.start2do.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.config.OpsConfig;
import org.start2do.dto.BusinessException;
import org.start2do.dto.R;
import org.start2do.dto.req.ops.OpsDeployReq;
import org.start2do.util.FileUtil;
import org.start2do.util.StringUtils;
import org.start2do.util.TOTPUtil;
import org.start2do.util.ZipUtil;

@Slf4j
@Controller
@RequestMapping("/ops")
@RequiredArgsConstructor
public class OpsController {

    private final OpsConfig config;

    @PostMapping("/deploy")
    public ResponseEntity<R<String>> deploy(@Valid OpsDeployReq req, @RequestHeader("X-TOTP") String totpCode) {
        if (StringUtils.isEmpty(totpCode) && StringUtils.isEmpty(req.getTotpCode())) {
            throw new BusinessException("TOTP密码不能为空");
        }
        if (StringUtils.isEmpty(req.getTotpCode()) && StringUtils.isNotEmpty(totpCode)) {
            log.warn("取Header的totpCode");
            req.setTotpCode(totpCode);
        }
        // 在方法体最开始处添加以下TOTP校验逻辑
        if (StringUtils.isEmpty(config.getTotpSecretKey())) {
            log.error("TOTP密钥未配置或使用的是不安全的占位符密钥。部署操作已中止。请配置安全的TOTP密钥。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.failed(HttpStatus.INTERNAL_SERVER_ERROR.value(), "TOTP服务配置错误，无法执行部署"));
        }

        if (!TOTPUtil.verifyTOTP(config.getTotpSecretKey(), req.getTotpCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(R.failed(HttpStatus.UNAUTHORIZED.value(), "TOTP验证失败"));
        }

        // 原有的路径白名单校验逻辑继续
        String destPath = req.getDestPath();
        if (!FileUtil.isPathAllowed(config.getWhitePath(), destPath)) {
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

            if (req.isClear()) {
                FileUtil.cleanTargetFolder(destPath);
            }

            MultipartFile multipartFile = req.getFile();
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
                    BufferedInputStream bis = new BufferedInputStream(inputStream);
                    bis.mark(4);
                    int bytesRead = bis.read(header);
                    bis.reset();
                    if (bytesRead == 4 && header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03
                        && header[3] == 0x04) {
                        isZip = true;
                    }
                }
            }
            boolean unzip = req.isUnzip();
            if (isZip && unzip) {
                try (InputStream inputStream = multipartFile.getInputStream()) {
                    ZipUtil.unzipStream(inputStream, destinationPath, req.getRootFileName());
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
            if (!FileUtil.isPathAllowed(config.getWhitePath(), path)) {
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
