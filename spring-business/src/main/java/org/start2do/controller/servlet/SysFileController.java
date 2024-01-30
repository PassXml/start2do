package org.start2do.controller.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.dto.R;
import org.start2do.dto.resp.file.SysFileUploadResp;
import org.start2do.entity.business.SysFile;
import org.start2do.service.servlet.SysFileService;

/**
 * 系统文件
 */
@RestController
@RequestMapping("/file")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "file", havingValue = "true")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysFileController {

    private final SysFileService sysFileService;
    private final BusinessConfig config;
    private final String uploadDir;

    public SysFileController(SysFileService sysFileService, BusinessConfig config) {
        this.sysFileService = sysFileService;
        this.config = config;
        if (config.getFileSetting() == null) {
            config.setFileSetting(new FileSetting());
        }
        uploadDir = config.getFileSetting().getUploadDir();
        File file = Paths.get(config.getFileSetting().getUploadDir()).toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 下载
     */
    @GetMapping("download")
    public void download(HttpServletResponse response, @RequestParam String fileId) throws IOException {
        SysFile file = sysFileService.getById(fileId);
        response.addHeader("Content-Disposition", "attachment;filename=" + file.getFileName());
        response.setContentType("application/octet-stream");
        switch (config.getFileSetting().getType()) {
            case local -> {
                try (FileInputStream inputStream = new FileInputStream(
                    Paths.get(uploadDir + File.separator + file.getFilePath()).toFile())) {
                    inputStream.transferTo(response.getOutputStream());
                }
            }
        }
    }

    /**
     * 根据路径下载
     */
    @RequestMapping(value = "download_proxy/**", method = RequestMethod.GET)
    public void downloadByPath(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI().replaceFirst("/file/download_proxy", "");
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setContentType("application/octet-stream");
        switch (config.getFileSetting().getType()) {
            case local -> {
                try (FileInputStream inputStream = new FileInputStream(
                    Paths.get(uploadDir + File.separator + path).toFile())) {
                    inputStream.transferTo(response.getOutputStream());
                }
            }
        }
    }

    /**
     * 上传
     */
    @PostMapping("upload")
    public R<SysFileUploadResp> upload(MultipartFile file) throws IOException {
        SysFile entity = sysFileService.updateFile(file);
        return R.ok(new SysFileUploadResp(entity.getId(), entity.getRelativeFilePath()));
    }
}
