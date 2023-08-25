package org.start2do.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.dto.R;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.service.SysFileService;
import org.start2do.util.Md5Util;

/**
 * 系统文件
 */
@RestController
@RequestMapping("/file")
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
                try (FileInputStream inputStream = new FileInputStream(Paths.get(file.getFilePath()).toFile())) {
                    inputStream.transferTo(response.getOutputStream());
                }
            }
        }
    }

    /**
     * 上传
     */
    @PostMapping("upload")
    public R<UUID> upload(MultipartFile file) throws IOException {
        long size = file.getSize();
        if (size < 1) {
            throw new RuntimeException("不加上传大小为空的文件");
        }
        String filename = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        String md5 = Md5Util.md5(inputStream);
        SysFile sysFile = sysFileService.findOne(new QSysFile().fileMd5.eq(md5));
        if (sysFile != null) {
            return R.ok(sysFile.getId());
        }
        Path path = Paths.get(uploadDir + "/" + md5);
        file.transferTo(path);
        SysFile entity = new SysFile(filename, path.toAbsolutePath().toString(), md5, null, size);
        sysFileService.save(entity);
        return R.ok(entity.getId());
    }
}
