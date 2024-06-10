package org.start2do.service.webflux;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
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
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.util.DateUtil;
import org.start2do.util.Md5Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysFileReactiveService extends AbsReactiveService<SysFile, Integer> implements CommandLineRunner {

    private final BusinessConfig businessConfig;
    private final QiNiuService qiNiuService;
    private Path uploadPath;

    public Mono<Boolean> removeFileById(String fileId) {
        return getById(fileId).publishOn(Schedulers.boundedElastic()).handle((sysFile, sink) -> {
            try {

                switch (businessConfig.getFileSetting().getType()) {
                    case local -> {
                        Files.delete(Paths.get(businessConfig.getFileSetting().getUploadDir() + sysFile.getFilePath()));
                    }
                    case qn -> {
                        qiNiuService.move(sysFile.getRelativeFilePath(),
                            "Recycle/".concat(sysFile.getRelativeFilePath()));
                    }
                }
            } catch (IOException e) {
                sink.error(e);
            } finally {
                sink.next(true);
            }
        });
    }

    private String getSubFix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public Mono<SysFile> uploadFile(String md5, String fileName, ByteArrayInputStream inputStream) {
        String finalMd5;
        if (md5 == null) {
            finalMd5 = Md5Util.md5(inputStream);
        } else {
            finalMd5 = md5;
        }
        return findOne(new QSysFile().fileMd5.eq(finalMd5)).switchIfEmpty(Mono.fromCallable(() -> {
            String uploadDir = businessConfig.getFileSetting().getUploadDir();
            String subfix = getSubFix(fileName);
            Path path = Paths.get(
                uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd") + File.separator
                    + finalMd5 + "." + subfix);
            Files.createDirectories(path.getParent());
            byte[] bytes = inputStream.readAllBytes();
            Files.write(path, bytes);
            String relativeFilePath = getRelativeFilePath(path);
            return new SysFile(fileName, relativeFilePath, relativeFilePath, finalMd5,
                businessConfig.getFileSetting().getHost(), (long) bytes.length, subfix);
        }).zipWhen(super::save).map(Tuple2::getT1));
    }

    private String getRelativeFilePath(Path path) {
        String string = uploadPath.relativize(path).toString();
        return string.replaceAll("\\\\", "/");
    }

    public static <R> Mono<DataBuffer> fileToBytes(FilePart part) {
        return DataBufferUtils.join(part.content());
    }

    public Mono<Void> download(ServerHttpResponse response, String fileId) {
        return super.getById(fileId).flatMap(sysFile -> {
            FileSetting fileSetting = businessConfig.getFileSetting();
            response.getHeaders().add("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
            response.getHeaders().add("Content-Type", "application/octet-stream");
            switch (fileSetting.getType()) {
                case local -> {

                    try (FileInputStream inputStream = new FileInputStream(
                        Paths.get(fileSetting.getUploadDir() + File.separator + sysFile.getFilePath()).toFile())) {
                        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.readByteChannel(inputStream::getChannel,
                            new DefaultDataBufferFactory(), 4096);
                        return response.writeWith(dataBufferFlux);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                case qn -> {
                    response.getHeaders().add("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
                    response.getHeaders().add("Content-Type", "application/octet-stream");
                    response.getHeaders().add("Location", sysFile.getUrl());
                    response.getHeaders().add("Connection", "close");
                }
            }
            return Mono.empty();
        });
    }

    public Mono<List<SysFile>> uploadFile(Boolean checkExist, FilePart... file) {
        List<Mono<SysFile>> result = new ArrayList<>();
        switch (businessConfig.getFileSetting().getType()) {
            case local -> {
                for (FilePart part : file) {
                    result.add(uploadByLocal(part));
                }
            }
            case qn -> {
                for (FilePart part : file) {
                    result.add(uploadByQn(part, checkExist));
                }
            }
        }
        //转化成result为 Mono<list<sysFile>>
        return Mono.zip(result, objects -> {
            List<SysFile> sysFiles = new ArrayList<>();
            for (Object object : objects) {
                sysFiles.add((SysFile) object);
            }
            return sysFiles;
        });
    }

    private Mono<SysFile> uploadByQn(FilePart part, boolean checkExist) {
        return Mono.from(fileToBytes(part).map(DataBuffer::asByteBuffer).map(ByteBuffer::array)).flatMap(bytes -> {
            String md5 = Md5Util.md5(bytes);
            long size = bytes.length;
            String subFix = getSubFix(part.filename());
            return Mono.just(checkExist).filter(aBoolean -> aBoolean)
                .flatMap(aBoolean -> super.findOne(new QSysFile().fileMd5.eq(md5)))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    String dateStr = DateUtil.LocalDateStr("yyyy/MM/dd");
                    return qiNiuService.upload(bytes, String.format("%s/%s.%s", dateStr, md5, subFix));
                }).flatMap(resp -> save(
                    new SysFile(part.filename(), resp.key, resp.key, md5, businessConfig.getFileSetting().getHost(),
                        size, subFix))));
        });
    }

    private Mono<SysFile> uploadByLocal(FilePart part) {
        return Mono.from(fileToBytes(part).map(DataBuffer::asByteBuffer).map(ByteBuffer::array)).flatMap(bytes -> {
            String md5 = Md5Util.md5(bytes);
            return super.findOne(new QSysFile().fileMd5.eq(md5))
                .switchIfEmpty(uploadFile(md5, part.filename(), new ByteArrayInputStream(bytes)));
        });
    }


    @Override
    public void run(String... args) throws Exception {
        Path path = Paths.get(businessConfig.getFileSetting().getUploadDir());
        File file = path.toFile();
        uploadPath = path;
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
