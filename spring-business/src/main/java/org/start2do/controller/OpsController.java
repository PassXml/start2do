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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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
import org.start2do.dto.req.ops.OpsRollbackReq;
import org.start2do.dto.resp.ops.BackupItemResp;
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

    /**
     * 备份目录到压缩文件
     * @param sourcePath 源目录路径
     * @return 备份文件路径
     * @throws IOException IO异常
     */
    private String backupDirectory(String sourcePath) throws IOException {
        if (StringUtils.isEmpty(config.getBakDir())) {
            log.warn("备份目录未配置，跳过备份");
            return null;
        }

        Path sourceDir = Paths.get(sourcePath);
        if (!Files.exists(sourceDir)) {
            log.info("源目录不存在，跳过备份: {}", sourcePath);
            return null;
        }

        // 创建备份目录
        Path backupDir = Paths.get(config.getBakDir());
        Files.createDirectories(backupDir);

        // 生成备份文件名：文件夹名称_时间.zip
        String dirName = sourceDir.getFileName().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = dirName + "_" + timestamp + ".zip";
        Path backupFilePath = backupDir.resolve(backupFileName);

        // 创建压缩备份
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupFilePath))) {
            ZipUtil.addDirectoryToZipNIO(zos, sourcePath, "");
        }

        log.info("备份成功: {} -> {}", sourcePath, backupFilePath);
        return backupFilePath.toString();
    }

    /**
     * 获取备份列表
     * @return 备份列表
     */
    private List<BackupItemResp> getBackupList() {
        List<BackupItemResp> backupList = new ArrayList<>();

        if (StringUtils.isEmpty(config.getBakDir())) {
            log.warn("备份目录未配置");
            return backupList;
        }

        Path backupDir = Paths.get(config.getBakDir());
        if (!Files.exists(backupDir)) {
            log.info("备份目录不存在: {}", config.getBakDir());
            return backupList;
        }

        try {
            List<Path> zipFiles = Files.list(backupDir)
                .filter(path -> path.toString().toLowerCase().endsWith(".zip"))
                .sorted(Comparator.<Path>comparingLong(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis();
                    } catch (IOException e) {
                        return 0L;
                    }
                }).reversed())
                .collect(Collectors.toList());
            for (Path zipFile : zipFiles) {
                try {
                    BackupItemResp backupItem = new BackupItemResp();
                    backupItem.setFileName(zipFile.getFileName().toString());
                    backupItem.setFilePath(zipFile.toString());
                    backupItem.setFileSize(Files.size(zipFile));
                    backupItem.setBackupTime(LocalDateTime.ofInstant(
                        Files.getLastModifiedTime(zipFile).toInstant(),
                        java.time.ZoneId.systemDefault()
                    ));
                    // 从文件名中提取原始目录名称
                    String fileName = zipFile.getFileName().toString();
                    if (fileName.endsWith(".zip")) {
                        String nameWithoutExt = fileName.substring(0, fileName.length() - 4);
                        // 格式: 原始目录名_时间戳
                        int underscoreIndex = nameWithoutExt.lastIndexOf('_');
                        if (underscoreIndex > 0) {
                            backupItem.setOriginalDirName(nameWithoutExt.substring(0, underscoreIndex));
                        } else {
                            backupItem.setOriginalDirName(nameWithoutExt);
                        }
                    }
                    backupList.add(backupItem);
                } catch (IOException e) {
                    log.error("读取备份文件信息失败: {}", zipFile, e);
                }
            }
        } catch (IOException e) {
            log.error("获取备份列表失败: {}", e.getMessage(), e);
        }

        return backupList;
    }

    /**
     * 执行回滚操作
     * @param backupFilePath 备份文件路径
     * @param targetPath 目标路径
     * @throws IOException IO异常
     */
    private void rollbackBackup(String backupFilePath, String targetPath) throws IOException {
        Path backupFile = Paths.get(backupFilePath);
        if (!Files.exists(backupFile)) {
            throw new IOException("备份文件不存在: " + backupFilePath);
        }

        Path targetDir = Paths.get(targetPath);
        // 如果目标目录存在，先清理
        if (Files.exists(targetDir)) {
            FileUtil.cleanTargetFolder(targetPath);
        }

        // 创建目标目录
        Files.createDirectories(targetDir);

        // 解压备份文件到目标目录
        try (InputStream inputStream = Files.newInputStream(backupFile)) {
            ZipUtil.unzipStream(inputStream, targetDir, "");
        }

        log.info("回滚成功: {} -> {}", backupFilePath, targetPath);
    }

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
            // 执行备份
            String backupPath = backupDirectory(destPath);
            if (backupPath != null) {
                log.info("备份完成: {}", backupPath);
            }

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

    /**
     * 获取备份列表
     * @param totpCode TOTP验证码
     * @return 备份列表
     */
    @GetMapping("/backup/list")
    public ResponseEntity<R<List<BackupItemResp>>> getBackupList(@RequestHeader("X-TOTP") String totpCode) {
        if (StringUtils.isEmpty(config.getTotpSecretKey())) {
            log.error("TOTP密钥未配置或使用的是不安全的占位符密钥。操作已中止。请配置安全的TOTP密钥。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.failed(HttpStatus.INTERNAL_SERVER_ERROR.value(), "TOTP服务配置错误"));
        }

        if (!TOTPUtil.verifyTOTP(config.getTotpSecretKey(), totpCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(R.failed(HttpStatus.UNAUTHORIZED.value(), "TOTP验证失败"));
        }

        List<BackupItemResp> backupList = getBackupList();
        return ResponseEntity.ok(R.ok(backupList));
    }

    /**
     * 执行回滚操作
     * @param req 回滚请求
     * @param totpCode TOTP验证码
     * @return 操作结果
     */
    @PostMapping("/rollback")
    public ResponseEntity<R<String>> rollback(@Valid OpsRollbackReq req, @RequestHeader("X-TOTP") String totpCode) {
        if (StringUtils.isEmpty(totpCode) && StringUtils.isEmpty(req.getTotpCode())) {
            throw new BusinessException("TOTP密码不能为空");
        }
        if (StringUtils.isEmpty(req.getTotpCode()) && StringUtils.isNotEmpty(totpCode)) {
            log.warn("取Header的totpCode");
            req.setTotpCode(totpCode);
        }

        if (StringUtils.isEmpty(config.getTotpSecretKey())) {
            log.error("TOTP密钥未配置或使用的是不安全的占位符密钥。回滚操作已中止。请配置安全的TOTP密钥。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.failed(HttpStatus.INTERNAL_SERVER_ERROR.value(), "TOTP服务配置错误"));
        }

        if (!TOTPUtil.verifyTOTP(config.getTotpSecretKey(), req.getTotpCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(R.failed(HttpStatus.UNAUTHORIZED.value(), "TOTP验证失败"));
        }

        // 验证备份文件路径是否在白名单内
        if (!FileUtil.isPathAllowed(config.getWhitePath(), req.getBackupPath())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(R.failed(HttpStatus.FORBIDDEN.value(),
                    String.format("备份文件路径 %s 不在白名单内", req.getBackupPath())));
        }

        try {
            // 从备份文件路径推断目标路径
            Path backupFile = Paths.get(req.getBackupPath());
            String backupFileName = backupFile.getFileName().toString();

            if (!backupFileName.endsWith(".zip")) {
                return ResponseEntity.badRequest().body(R.failed("备份文件必须是.zip格式"));
            }

            // 从备份文件名中提取原始目录名
            String nameWithoutExt = backupFileName.substring(0, backupFileName.length() - 4);
            String originalDirName = nameWithoutExt;
            int underscoreIndex = nameWithoutExt.lastIndexOf('_');
            if (underscoreIndex > 0) {
                originalDirName = nameWithoutExt.substring(0, underscoreIndex);
            }

            // 构造目标路径（假设目标路径在白名单中）
            String targetPath = config.getWhitePath().get(0) + "/" + originalDirName;

            // 验证目标路径是否在白名单内
            if (!FileUtil.isPathAllowed(config.getWhitePath(), targetPath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(
                        R.failed(HttpStatus.FORBIDDEN.value(), String.format("目标路径 %s 不在白名单内", targetPath)));
            }

            // 执行回滚
            rollbackBackup(req.getBackupPath(), targetPath);

            return ResponseEntity.ok(R.ok("回滚成功到路径: " + targetPath));

        } catch (IOException e) {
            log.error("回滚失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.failed("回滚失败: " + e.getMessage()));
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
