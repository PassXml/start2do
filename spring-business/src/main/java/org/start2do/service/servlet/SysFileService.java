package org.start2do.service.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.dto.file.FileUpdateByteDto;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysFile;
import org.start2do.service.IFileOperationService;

@Slf4j
@Service
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "file", havingValue = "true", matchIfMissing = true)
public class SysFileService extends AbsService<SysFile> {

    private final BusinessConfig businessConfig;
    private Path uploadPath;

    @Lazy
    @Resource
    private IFileOperationService operationService;

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

    public SysFile updateFile(String fileName, byte[] byteArray) {
        return operationService.upload(byteArray, fileName, true);
    }

    public List<SysFile> uploadFile(Boolean checkExist, FileUpdateByteDto... dtos) {
        List<SysFile> result = new ArrayList<>();
        for (FileUpdateByteDto dto : dtos) {
            result.add(operationService.upload(dto.getBytes(), dto.getFileName(), checkExist));
        }
        //转化成result为 Mono<list<sysFile>>
        List<SysFile> sysFiles = new ArrayList<>();
        for (Object object : dtos) {
            sysFiles.add((SysFile) object);
        }
        return sysFiles;
    }

    public SysFile updateFile(String fileName, ByteArrayOutputStream outputStream) {
        return updateFile(fileName, outputStream.toByteArray());
    }

    public String getRelativeFilePath(Path path) {
        String string = uploadPath.relativize(path).toString();
        return string.replaceAll("\\\\", "/");
    }

    public SysFile updateFile(MultipartFile file,boolean replace) throws IOException {
        return operationService.upload(file.getBytes(), file.getOriginalFilename(), replace);
    }
}
