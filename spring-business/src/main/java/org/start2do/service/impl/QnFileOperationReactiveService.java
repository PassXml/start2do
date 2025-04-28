package org.start2do.service.impl;

import java.nio.ByteBuffer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.start2do.BusinessConfig;
import org.start2do.dto.BusinessException;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.service.IFileMd5;
import org.start2do.service.IFileOperationHookService;
import org.start2do.service.IFileOperationService;
import org.start2do.service.webflux.SysFileReactiveService;
import org.start2do.util.DateUtil;
import org.start2do.util.Md5Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.file-setting", name = "type", havingValue = "qn")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class QnFileOperationReactiveService implements IFileOperationService {

    private final BusinessConfig businessConfig;
    private final QiNiuService qiNiuService;
    private final SysFileReactiveService fileReactiveService;
    private final IFileMd5 fileMd5;
    private final IFileOperationHookService hookService;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Boolean> removeReactive(String fileId) {
        return fileReactiveService.findOneReactive(new QSysFile().id.eq(fileId)).map(sysFile -> {
            String relativeFilePath = sysFile.getRelativeFilePath();
            qiNiuService.move(relativeFilePath, "Recycle/".concat(relativeFilePath));
            return sysFile;
        }).flatMap(fileReactiveService::deleteReactive).thenReturn(true);

    }

    @Override
    public Mono<SysFile> uploadReactive(FilePart part, Boolean checkExist) {
        return Mono.from(fileToBytes(part).map(DataBuffer::asByteBuffer).map(ByteBuffer::array)).flatMap(bytes -> {
            String md5 = fileMd5.md5(bytes);
            long size = bytes.length;
            String subFix = getSubFix(part.filename());
            return Mono.just(checkExist).filter(aBoolean -> aBoolean)
                .flatMap(aBoolean -> fileReactiveService.findOneReactive(new QSysFile().fileMd5.eq(md5)))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    String dateStr = DateUtil.LocalDateStr("yyyy/MM/dd");
                    return qiNiuService.upload(bytes, String.format("%s/%s.%s", dateStr, md5, subFix));
                }).flatMap(defaultPutRet -> fileReactiveService.saveReactive(
                    new SysFile(part.filename(), defaultPutRet.key, defaultPutRet.key, md5,
                        businessConfig.getFileSetting().getHost(), size, subFix)))).map(file -> {
                    hookService.uploadAfter(bytes, file);
                    return file;
                });
        });
    }

    @Override
    public Mono<SysFile> uploadReactive(byte[] bytes, String fileName, Boolean checkExist) {
        return Mono.fromCallable(() -> {
            String md5 = Md5Util.md5(bytes);
            long size = bytes.length;
            String subFix = getSubFix(fileName);
            return Mono.just(checkExist).filter(aBoolean -> aBoolean)
                .flatMap(aBoolean -> fileReactiveService.findOneReactive(new QSysFile().fileMd5.eq(md5)))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    String dateStr = DateUtil.LocalDateStr("yyyy/MM/dd");
                    return qiNiuService.upload(bytes, String.format("%s/%s.%s", dateStr, md5, subFix));
                }).flatMap(defaultPutRet -> fileReactiveService.saveReactive(
                    new SysFile(fileName, defaultPutRet.key, defaultPutRet.key, md5,
                        businessConfig.getFileSetting().getHost(), size, subFix))));
        }).flatMap(Function.identity()).map(file -> {
            hookService.uploadAfter(bytes, file);
            return file;
        });
    }

    @Override
    public SysFile upload(byte[] bytes, String fileName, Boolean checkExist) {
        throw new BusinessException("不支持同步上传");
    }

    @Override
    public Mono<Boolean> downloadReactive(ServerHttpResponse response, String fileId) {
        return fileReactiveService.getByIdReactive(fileId).flatMap(sysFile -> {
            // 设置响应头
            response.getHeaders().add("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
            response.getHeaders().add("Content-Type", "application/octet-stream");
            response.getHeaders().add("Connection", "close");
            // 使用 WebClient 从文件的 URL 获取数据流
            Flux<DataBuffer> dataBufferFlux = webClient.get()
                .uri(sysFile.getUrl())
                .retrieve()
                .bodyToFlux(DataBuffer.class);
            // 将数据流写入响应
            return response.writeWith(dataBufferFlux)
                .then(Mono.just(true))
                .onErrorResume(throwable -> {
                    // 如果发生错误，返回 false 或处理错误
                    return Mono.just(false);
                });
        });
    }
}
