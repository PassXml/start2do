package org.start2do.service.impl;

import com.qiniu.storage.model.DefaultPutRet;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            log.info("开始下载文件，文件ID: {}, 文件名: {}", fileId, sysFile.getFileName());
            // 设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Connection", "close");
            // 建立远程连接
            URL url = new URL(sysFile.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            // 获取远程文件大小（如果有Content-Length头）
            long remoteFileSize = connection.getContentLengthLong();
            if (remoteFileSize > 0) {
                response.setContentLengthLong(remoteFileSize);
                log.info("远程文件大小: {} bytes", remoteFileSize);
            } else {
                log.warn("远程服务器未提供文件大小信息");
            }
            // 获取输入流和输出流
            inputStream = connection.getInputStream();
            outputStream = response.getOutputStream();
            // 流式传输数据
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            outputStream.flush();
            // 校验传输的文件大小是否一致
            if (remoteFileSize > 0 && totalBytesRead != remoteFileSize) {
                log.error("文件传输不完整，预期大小: {}, 实际传输大小: {}", remoteFileSize, totalBytesRead);
                return false;
            }
            log.info("文件下载完成，文件ID: {}, 传输大小: {} bytes", fileId, totalBytesRead);
            return true;
        } catch (Exception e) {
            log.error("文件下载失败，文件ID: {}, 错误信息: {}", fileId, e.getMessage(), e);
            return false;
        } finally {
            // 确保资源关闭
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭资源时发生错误，文件ID: {}, 错误信息: {}", fileId, e.getMessage(), e);
            }
        }
    }
}
