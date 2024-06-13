package org.start2do.service.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.util.DateUtil;
import org.start2do.util.Md5Util;

@Service
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysFileService extends AbsService<SysFile> {

    private final BusinessConfig businessConfig;
    private Path uploadPath;

    public SysFileService(BusinessConfig config) {
        this.businessConfig = config;
        if (config.getFileSetting() == null) {
            config.setFileSetting(new FileSetting());
        }
        Path path = Paths.get(config.getFileSetting().getUploadDir());
        uploadPath = path;
        File file = path.toFile();
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

    public SysFile updateFile(String fileName, ByteArrayOutputStream outputStream) throws IOException {
        byte[] byteArray = outputStream.toByteArray();
        String md5 = Md5Util.md5(new ByteArrayInputStream(byteArray));
        SysFile sysFile = findOne(new QSysFile().fileMd5.eq(md5));
        if (sysFile != null) {
            return sysFile;
        }
        String uploadDir = businessConfig.getFileSetting().getUploadDir();
        String subfix = fileName.substring(fileName.lastIndexOf(".") + 1);
        Path path = Paths.get(
            uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd") + File.separator + md5
                + "." + subfix);
        Files.createDirectories(path.getParent());
        Files.write(path, byteArray);
        String relativeFilePath = getRelativeFilePath(path);
        SysFile entity = new SysFile(fileName, relativeFilePath, relativeFilePath, md5,
            businessConfig.getFileSetting().getHost(), (long) byteArray.length, subfix);
        super.save(entity);
        return entity;
    }

    public String getRelativeFilePath(Path path) {
        String string = uploadPath.relativize(path).toString();
        return string.replaceAll("\\\\", "/");
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
        String uploadDir = businessConfig.getFileSetting().getUploadDir();
        String subfix = filename.substring(filename.lastIndexOf(".") + 1);
        Path path = Paths.get(
            uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd") + File.separator + md5
                + "." + subfix);
        Files.createDirectories(path.getParent());
        file.transferTo(path);
        String relativeFilePath = getRelativeFilePath(path);
        SysFile entity = new SysFile(filename, relativeFilePath, relativeFilePath, md5,
            businessConfig.getFileSetting().getHost(), size, subfix);
        super.save(entity);
        return entity;
    }
}
