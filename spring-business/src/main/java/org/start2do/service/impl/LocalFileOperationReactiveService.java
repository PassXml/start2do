package org.start2do.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.file.FileUpdateResultDto;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.service.IFileMd5;
import org.start2do.service.IFileOperationHookService;
import org.start2do.service.IFileOperationService;
import org.start2do.service.webflux.SysFileReactiveService;
import org.start2do.util.LocalFileUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.file-setting", name = "type", havingValue = "local")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class LocalFileOperationReactiveService implements IFileOperationService {

    private final BusinessConfig businessConfig;
    private final SysFileReactiveService sysFileReactiveService;
    private final IFileOperationHookService hookService;
    private final IFileMd5 fileMd5;

    private SysFile uploadFile(String md5, String fileName, ByteArrayInputStream inputStream) {
        FileUpdateResultDto dto = LocalFileUtils.upload(businessConfig.getFileSetting().getUploadDir(), md5, fileName,
            inputStream);
        return new SysFile(fileName, dto.getFullPath(), dto.getRelativePath(), dto.getMd5(),
            businessConfig.getFileSetting().getHost(), dto.getSize(), dto.getSuffix());
    }

    @Override
    public Mono<SysFile> uploadReactive(FilePart part, Boolean checkExist) {
        throw new BusinessException("not support");
    }

    @Override
    public Mono<SysFile> uploadReactive(byte[] bytes, String fileName, Boolean checkExist) {
        byte[] before = hookService.uploadBefore(bytes);
        return Mono.fromCallable(() -> {
            String md5 = fileMd5.md5(before);
            if (checkExist) {
                return sysFileReactiveService.findOneReactive(new QSysFile().fileMd5.eq(md5)).switchIfEmpty(
                    Mono.just(uploadFile(md5, fileName, new ByteArrayInputStream(bytes)))
                        .flatMap(sysFileReactiveService::saveReactive)).map(file -> {
                    hookService.uploadAfter(bytes, file);
                    return file;
                });
            } else {
                return Mono.just(uploadFile(md5, fileName, new ByteArrayInputStream(bytes)))
                    .flatMap(sysFileReactiveService::saveReactive).map(file -> {
                        hookService.uploadAfter(bytes, file);
                        return file;
                    });
            }
        }).flatMap(Function.identity());
    }

    @Override
    public SysFile upload(byte[] bytes, String fileName, Boolean checkExist) {
        return null;
    }

    @Override
    public Mono<Boolean> downloadReactive(ServerHttpResponse response, String fileId) {
        FileSetting fileSetting = businessConfig.getFileSetting();
        return sysFileReactiveService.findOneByIdReactive(fileId).flatMap(sysFile -> {
            File file = Paths.get(fileSetting.getUploadDir() + File.separator + sysFile.getFilePath()).toFile();
            if (!file.exists()) {
                throw new BusinessException("文件不存在");
            }
            HttpHeaders headers = response.getHeaders();
            headers.add("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
            headers.add("Content-Type", "application/octet-stream");
            String fileSize = Optional.ofNullable(sysFile.getFileSize()).map(Long::intValue).map(String::valueOf)
                .orElse("0");
            if ("0".equals(fileSize)) {
                File f = Paths.get(sysFile.getFilePath()).toFile();
                if (f.exists()) {
                    fileSize = String.valueOf(f.length());
                }
            }
            headers.add("Content-Length", fileSize);
            try (FileInputStream inputStream = new FileInputStream(file)) {
                Flux<DataBuffer> dataBufferFlux = DataBufferUtils.readByteChannel(inputStream::getChannel,
                    new DefaultDataBufferFactory(), 4096);
                return response.writeWith(dataBufferFlux);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).map(unused -> true).switchIfEmpty(Mono.error(new DataNotFoundException()));
    }
}
