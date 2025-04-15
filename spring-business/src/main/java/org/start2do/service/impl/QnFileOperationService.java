package org.start2do.service.impl;

import com.qiniu.storage.model.DefaultPutRet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.service.IFileMd5;
import org.start2do.service.IFileOperationHookService;
import org.start2do.service.IFileOperationService;
import org.start2do.service.webflux.SysFileReactiveService;
import org.start2do.util.DateUtil;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.file-setting", name = "type", havingValue = "qn")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class QnFileOperationService implements IFileOperationService {

    private final BusinessConfig businessConfig;
    private final QiNiuService qiNiuService;
    private final SysFileReactiveService fileReactiveService;
    private final IFileMd5 fileMd5;
    private final IFileOperationHookService hookService;

    @Override
    public boolean remove(String fileId) {
        SysFile sysFile = fileReactiveService.findOne(new QSysFile().id.eq(fileId));
        String relativeFilePath = sysFile.getRelativeFilePath();
        qiNiuService.move(relativeFilePath, "Recycle/".concat(relativeFilePath));
        fileReactiveService.delete(sysFile);
        return true;
    }

    @Override
    public SysFile upload(byte[] bytes, String fileName, Boolean checkExist) {
        String md5 = fileMd5.md5(bytes);
        long size = bytes.length;
        String subFix = getSubFix(fileName);

        if (checkExist) {
            SysFile existingFile = fileReactiveService.findOne(new QSysFile().fileMd5.eq(md5));
            if (existingFile != null) {
                return existingFile;
            }
        }

        String dateStr = DateUtil.LocalDateStr("yyyy/MM/dd");
        DefaultPutRet result = qiNiuService.upload(bytes, String.format("%s/%s.%s", dateStr, md5, subFix));
        SysFile newFile = new SysFile(fileName, result.key, result.key, md5,
            businessConfig.getFileSetting().getHost(), size, subFix);
        fileReactiveService.save(newFile);
        hookService.uploadAfter(bytes, newFile);
        return newFile;
    }

    @Override
    public boolean download(HttpServletResponse response, String fileId) {
        SysFile sysFile = fileReactiveService.getById(fileId);
        response.setHeader("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Location", sysFile.getUrl());
        response.setHeader("Connection", "close");
        return true;
    }
}
