package org.start2do.service.webflux.impl;

import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.service.webflux.IFileOperationService;
import org.start2do.service.webflux.QiNiuService;
import org.start2do.service.webflux.SysFileReactiveService;
import org.start2do.util.DateUtil;
import org.start2do.util.Md5Util;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.file-setting", name = "type", havingValue = "qn")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class QnFileOperationService implements IFileOperationService {

    private final BusinessConfig businessConfig;
    private final QiNiuService qiNiuService;
    private final SysFileReactiveService fileReactiveService;

    @Override
    public Mono remove(String fileId) {
        return fileReactiveService.findOne(new QSysFile().idStr.eq(fileId)).map(sysFile -> {
            String relativeFilePath = sysFile.getRelativeFilePath();
            qiNiuService.move(relativeFilePath, "Recycle/".concat(relativeFilePath));
            return sysFile;
        }).flatMap(fileReactiveService::delete);

    }

    @Override
    public Mono<SysFile> update(FilePart part, Boolean checkExist) {
        return Mono.from(fileToBytes(part).map(DataBuffer::asByteBuffer).map(ByteBuffer::array)).flatMap(bytes -> {
            String md5 = Md5Util.md5(bytes);
            long size = bytes.length;
            String subFix = getSubFix(part.filename());
            return Mono.just(checkExist).filter(aBoolean -> aBoolean)
                .flatMap(aBoolean -> fileReactiveService.findOne(new QSysFile().fileMd5.eq(md5)))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    String dateStr = DateUtil.LocalDateStr("yyyy/MM/dd");
                    return qiNiuService.upload(bytes, String.format("%s/%s.%s", dateStr, md5, subFix));
                }).flatMap(defaultPutRet -> fileReactiveService.save(
                    new SysFile(part.filename(), defaultPutRet.key, defaultPutRet.key, md5,
                        businessConfig.getFileSetting().getHost(), size, subFix))));
        });
    }

    @Override
    public Mono<Boolean> download(ServerHttpResponse response, String fileId) {
        return fileReactiveService.getById(fileId).flatMap(sysFile -> {
            response.getHeaders().add("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
            response.getHeaders().add("Content-Type", "application/octet-stream");
            response.getHeaders().add("Location", sysFile.getUrl());
            response.getHeaders().add("Connection", "close");
            return Mono.just(true);
        });
    }
}
