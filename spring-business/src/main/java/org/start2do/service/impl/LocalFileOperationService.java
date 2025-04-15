package org.start2do.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.dto.file.FileUpdateResultDto;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.service.IFileMd5;
import org.start2do.service.IFileOperationHookService;
import org.start2do.service.IFileOperationService;
import org.start2do.service.servlet.SysFileService;
import org.start2do.util.LocalFileUtils;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.file-setting", name = "type", havingValue = "local")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class LocalFileOperationService implements IFileOperationService {

    private final BusinessConfig businessConfig;
    private final SysFileService sysFileService;
    private final IFileOperationHookService hookService;
    private final IFileMd5 fileMd5;

    @Override
    public boolean remove(String fileId) {
        SysFile sysFile = sysFileService.findOne(new QSysFile().id.eq(fileId));
        String relativeFilePath = sysFile.getRelativeFilePath();
        LocalFileUtils.move(businessConfig.getFileSetting().getUploadDir(), relativeFilePath,
            "Recycle/".concat(relativeFilePath));
        sysFileService.delete(sysFile);
        return true;
    }

    private SysFile uploadFile(String md5, String fileName, ByteArrayInputStream inputStream) {
        FileUpdateResultDto dto = LocalFileUtils.upload(businessConfig.getFileSetting().getUploadDir(), md5, fileName,
            inputStream);
        return new SysFile(fileName, dto.getFullPath(), dto.getRelativePath(), dto.getMd5(),
            businessConfig.getFileSetting().getHost(), dto.getSize(), dto.getSuffix());
    }


    @Override
    public SysFile upload(byte[] bytes, String fileName, Boolean checkExist) {
        byte[] before = hookService.uploadBefore(bytes);
        String md5 = fileMd5.md5(before);
        if (checkExist) {
            SysFile sysFile = sysFileService.findOne(new QSysFile().fileMd5.eq(md5));
            if (sysFile == null) {
                sysFile = uploadFile(md5, fileName, new ByteArrayInputStream(bytes));
                sysFileService.save(sysFile);
            }
            hookService.uploadAfter(bytes, sysFile);
            return sysFile;
        } else {
            SysFile sysFile = uploadFile(md5, fileName, new ByteArrayInputStream(bytes));
            sysFileService.save(sysFile);
            hookService.uploadAfter(bytes, sysFile);
            return sysFile;
        }
    }

    @Override
    public boolean download(HttpServletResponse response, String fileId) {
        FileSetting fileSetting = businessConfig.getFileSetting();
        SysFile sysFile = sysFileService.findOneById(fileId);
        response.setHeader("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
        response.setContentType("application/octet-stream");
        try (FileInputStream inputStream = new FileInputStream(
            Paths.get(fileSetting.getUploadDir() + File.separator + sysFile.getFilePath())
                .toFile()); OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
