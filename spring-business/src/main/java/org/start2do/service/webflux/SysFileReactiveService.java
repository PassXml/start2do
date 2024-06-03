package org.start2do.service.webflux;

import com.qiniu.storage.model.DefaultPutRet;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.multipart.MultipartFile;
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
import reactor.util.function.Tuples;

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

    public Mono<SysFile> uploadFile(String fileName, ByteArrayInputStream inputStream) {
        String md5 = Md5Util.md5(inputStream);
        return findOne(new QSysFile().fileMd5.eq(md5)).switchIfEmpty(Mono.fromCallable(() -> {
            String uploadDir = businessConfig.getFileSetting().getUploadDir();
            String subfix = getSubFix(fileName);
            Path path = Paths.get(
                uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd") + File.separator
                    + md5 + "." + subfix);
            Files.createDirectories(path.getParent());
            byte[] bytes = inputStream.readAllBytes();
            Files.write(path, bytes);
            String relativeFilePath = getRelativeFilePath(path);
            return new SysFile(fileName, relativeFilePath, relativeFilePath, md5,
                businessConfig.getFileSetting().getHost(), (long) bytes.length, subfix);
        }).zipWhen(super::save).map(Tuple2::getT1));
    }

    public String getRelativeFilePath(Path path) {
        String string = uploadPath.relativize(path).toString();
        return string.replaceAll("\\\\", "/");
    }

    public Mono<SysFile> uploadFile(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        String md5 = Md5Util.md5(inputStream);
        return Mono.just(file).filter(multipartFile -> multipartFile.getSize() > 1)
            .switchIfEmpty(Mono.error(new RuntimeException("不加上传大小为空的文件")))
            .flatMap(objects -> findOne(new QSysFile().fileMd5.eq(md5))).switchIfEmpty(Mono.fromCallable(() -> {
                String filename = file.getOriginalFilename();
                String uploadDir = businessConfig.getFileSetting().getUploadDir();
                String subfix = filename.substring(filename.lastIndexOf(".") + 1);
                Path path = Paths.get(
                    uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd")
                        + File.separator + md5 + "." + subfix);
                Files.createDirectories(path.getParent());
                file.transferTo(path);
                String relativeFilePath = getRelativeFilePath(path);
                return new SysFile(filename, relativeFilePath, relativeFilePath, md5,
                    businessConfig.getFileSetting().getHost(), file.getSize(), subfix);
            }).zipWhen(super::save).map(Tuple2::getT1));
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

    public Mono<List<SysFile>> uploadFile(FilePart... file) {
        List<Mono<SysFile>> result = new ArrayList<>();
        switch (businessConfig.getFileSetting().getType()) {
            case local -> {
                for (FilePart part : file) {
                    Mono<SysFile> mono = fileToBytes(part).map(DataBuffer::asByteBuffer).map(ByteBuffer::array)
                        .flatMap(bytes -> uploadFile(part.filename(), new ByteArrayInputStream(bytes)));
                    result.add(mono);
                }
            }
            case qn -> {
                for (FilePart part : file) {
                    result.add(uploadByQn(part));
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

    private Mono<SysFile> uploadByQn(FilePart part) {
        return fileToBytes(part).map(DataBuffer::asByteBuffer).map(ByteBuffer::array).zipWhen(
                bytes -> Mono.fromCallable(
                    () -> Tuples.of(Md5Util.md5(bytes), bytes.length, getSubFix(part.filename()))))
            .map(objects -> {
                String dateStr = DateUtil.LocalDateStr("yyyy/MM/dd");
                byte[] bytes = objects.getT1();
                return Tuples.of(qiNiuService.upload(bytes,
                        String.format("%s/%s.%s", dateStr, objects.getT2().getT1(), objects.getT2().getT3())),
                    objects.getT2());
            }).flatMap(objects -> {
                String Md5 = objects.getT2().getT1();
                long size = objects.getT2().getT2();
                String subFix = objects.getT2().getT3();
                DefaultPutRet resp = objects.getT1();
                return save(new SysFile(part.filename(), resp.key, resp.key, Md5,
                    businessConfig.getFileSetting().getHost(), size, subFix));
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
