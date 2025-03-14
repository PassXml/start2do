package org.start2do.ops.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.start2do.dto.BusinessException;
import org.start2do.dto.R;
import org.start2do.ops.config.OpsConfig;
import org.start2do.ops.dto.deploy.DeployReq;
import org.start2do.ops.service.DeployService;
import org.start2do.util.FileUtil;
import org.start2do.util.ZipUtil;

@Slf4j
@Controller
@RequestMapping("deploy")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "org.start2do.ops", name = "enable")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class DeployController {

    private final OpsConfig opsConfig;
    private final DeployService deployService;

    @PostMapping({"", "/"})
    public R deploy(@Valid DeployReq req) throws IOException {
        if (!FileUtil.isPathAllowed(opsConfig.getWhitePath(), req.getDestPath())) {
            throw new BusinessException("不在白名单内");
        }
        deployService.deploy(req.getDestPath(), req.getFile().getInputStream(), req.getRootFileName());
        return R.ok();
    }

    @GetMapping("/download")
    public void downloadFiles(@RequestParam List<String> paths, HttpServletResponse response) {
        try {
            // 验证路径是否允许访问
            for (String path : paths) {
                if (!FileUtil.isPathAllowed(opsConfig.getWhitePath(), path)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Path access not allowed: " + path);
                }
            }
            // 设置响应头
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"files.zip\"");
            // 使用 ZipOutputStream 打包文件
            try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(response.getOutputStream())) {
                for (String path : paths) {
                    File file = new File(path);
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            ZipUtil.addDirectoryToZip(file, "", zipOut);
                        } else {
                            ZipUtil.addFileToZip(file, "", zipOut);
                        }
                    } else {
                        log.warn("Path not found: {}", path);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while downloading files: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("Error: " + e.getMessage());
            } catch (IOException ex) {
                log.error("Failed to write error response: {}", ex.getMessage());
            }
        }
    }

}
