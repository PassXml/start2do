package org.start2do.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.util.Md5Util;

@Service
public class SysFileService extends AbsService<SysFile> {

    private final BusinessConfig businessConfig;

    public SysFileService(BusinessConfig config) {
        this.businessConfig = config;
        if (config.getFileSetting() == null) {
            config.setFileSetting(new FileSetting());
        }
        File file = Paths.get(config.getFileSetting().getUploadDir()).toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public void removeFileById(String fileId) {
        SysFile file = getById(fileId);
        try {
            Files.delete(Paths.get(businessConfig.getFileSetting().getUploadDir() + file.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SysFile updateFile(MultipartFile file) throws IOException {
        long size = file.getSize();
        if (size < 1) {
            throw new RuntimeException("不加上传大小为空的文件");
        }
        String filename = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        String md5 = Md5Util.md5(inputStream);
        SysFile sysFile = findOne(new QSysFile().fileMd5.eq(md5));
        if (sysFile != null) {
            return sysFile;
        }
        Path path = Paths.get(businessConfig.getFileSetting().getUploadDir() + "/" + md5);
        file.transferTo(path);
        String filePath = path.toAbsolutePath().toString();
        SysFile entity = new SysFile(filename, filePath,
            md5, md5, null, size);
        super.save(entity);
        return entity;
    }
}
