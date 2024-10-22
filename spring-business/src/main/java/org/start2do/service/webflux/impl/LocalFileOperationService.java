package org.start2do.service.webflux.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.service.IFileMd5;
import org.start2do.service.webflux.IFileOperationService;
import org.start2do.service.webflux.SysFileReactiveService;
import org.start2do.util.DateUtil;
import org.start2do.util.Md5Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.file-setting", name = "type", havingValue = "local")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class LocalFileOperationService implements IFileOperationService {

    private final BusinessConfig businessConfig;
    private final SysFileReactiveService sysFileReactiveService;
    private final IFileMd5 fileMd5;

    @Override
    public Mono remove(String fileId) {
        return null;
    }

    public SysFile uploadFile(String md5, String fileName, ByteArrayInputStream inputStream) {
        String finalMd5;
        if (md5 == null) {
            finalMd5 = Md5Util.md5(inputStream);
        } else {
            finalMd5 = md5;
        }
        String uploadDir = businessConfig.getFileSetting().getUploadDir();
        String subfix = getSubFix(fileName);
        Path path = Paths.get(
            uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd") + File.separator
            + finalMd5 + "." + subfix);
        try {
            Files.createDirectories(path.getParent());
            byte[] bytes = inputStream.readAllBytes();
            File file = path.toFile();
            //文件不存在
            if (!file.exists()) {
                Files.write(path, bytes);
            }
            String relativeFilePath = getRelativeFilePath(Paths.get(businessConfig.getFileSetting().getUploadDir()),
                path);
            return new SysFile(fileName, path.toString(), relativeFilePath, finalMd5,
                businessConfig.getFileSetting().getHost(), Integer.valueOf(bytes.length).longValue(), subfix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<SysFile> update(FilePart part, Boolean checkExist) {
        return Mono.from(fileToBytes(part).map(DataBuffer::asByteBuffer).map(ByteBuffer::array)).flatMap(bytes -> {
            String md5 = fileMd5.md5(bytes);
            ;
            if (checkExist) {
                return sysFileReactiveService.findOne(new QSysFile().fileMd5.eq(md5)).switchIfEmpty(
                    Mono.just(uploadFile(md5, part.filename(), new ByteArrayInputStream(bytes)))
                        .flatMap(sysFileReactiveService::save));
            } else {
                return Mono.just(uploadFile(md5, part.filename(), new ByteArrayInputStream(bytes)))
                    .zipWhen(file -> sysFileReactiveService.findOne(new QSysFile().fileMd5.eq(md5)))
                    .flatMap(objects -> {
                        SysFile newFile = objects.getT1();
                        SysFile oldFile = objects.getT2();
                        //更新oldFile的属性,从newfile读取
                        oldFile.setFileName(newFile.getFileName());
                        oldFile.setFilePath(newFile.getFilePath());
                        oldFile.setFileMd5(newFile.getFileMd5());
                        oldFile.setFileSize(newFile.getFileSize());
                        oldFile.setRelativeFilePath(newFile.getRelativeFilePath());
                        oldFile.setSuffix(newFile.getSuffix());
                        return sysFileReactiveService.update(oldFile);
                    });
            }
        });
    }

    @Override
    public Mono<SysFile> update(byte[] bytes, String fileName, Boolean checkExist) {
        return Mono.fromCallable(() -> {
            String md5 = Md5Util.md5(bytes);
            if (checkExist) {
                return sysFileReactiveService.findOne(new QSysFile().fileMd5.eq(md5)).switchIfEmpty(
                    Mono.just(uploadFile(md5, fileName, new ByteArrayInputStream(bytes)))
                        .flatMap(sysFileReactiveService::save));
            } else {
                return Mono.just(uploadFile(md5, fileName, new ByteArrayInputStream(bytes)))
                    .flatMap(sysFileReactiveService::save);
            }
        }).flatMap(Function.identity());
    }

    @Override
    public Mono<Boolean> download(ServerHttpResponse response, String fileId) {
        FileSetting fileSetting = businessConfig.getFileSetting();
        return sysFileReactiveService.findOneById(fileId).flatMap(sysFile -> {
            response.getHeaders().add("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
            response.getHeaders().add("Content-Type", "application/octet-stream");
            try (FileInputStream inputStream = new FileInputStream(
                Paths.get(fileSetting.getUploadDir() + File.separator + sysFile.getFilePath()).toFile())) {
                Flux<DataBuffer> dataBufferFlux = DataBufferUtils.readByteChannel(inputStream::getChannel,
                    new DefaultDataBufferFactory(), 4096);
                return response.writeWith(dataBufferFlux);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).map(unused -> true);
    }
}
