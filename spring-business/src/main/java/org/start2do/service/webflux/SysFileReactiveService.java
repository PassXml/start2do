package org.start2do.service.webflux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.business.SysFile;
import org.start2do.entity.business.query.QSysFile;
import org.start2do.util.DateUtil;
import org.start2do.util.Md5Util;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

@Service
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysFileReactiveService extends AbsReactiveService<SysFile, Integer> {

    private final BusinessConfig businessConfig;
    private Path uploadPath;

    public SysFileReactiveService(BusinessConfig config) {
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


    public Mono<Boolean> removeFileById(String fileId) {
        return getById(fileId).publishOn(Schedulers.boundedElastic()).handle((sysFile, synchronousSink) -> {
            try {
                Files.delete(Paths.get(businessConfig.getFileSetting().getUploadDir() + sysFile.getFilePath()));
            } catch (IOException e) {
                synchronousSink.error(e);
            } finally {
                synchronousSink.next(true);
            }
        });
    }

    public Mono<SysFile> updateFile(String fileName, ByteArrayOutputStream outputStream) throws IOException {
        byte[] byteArray = outputStream.toByteArray();
        String md5 = Md5Util.md5(new ByteArrayInputStream(byteArray));
        return findOne(new QSysFile().fileMd5.eq(md5)).switchIfEmpty(Mono.fromCallable(() -> {
            String uploadDir = businessConfig.getFileSetting().getUploadDir();
            String subfix = fileName.substring(fileName.lastIndexOf(".") + 1);
            Path path = Paths.get(
                uploadDir + File.separator + DateUtil.LocalDateToString(LocalDate.now(), "yyyyMMdd") + File.separator
                    + md5 + "." + subfix);
            Files.createDirectories(path.getParent());
            Files.write(path, byteArray);
            String relativeFilePath = getRelativeFilePath(path);
            return new SysFile(fileName, relativeFilePath, relativeFilePath, md5,
                businessConfig.getFileSetting().getHost(), (long) byteArray.length, subfix);
        }).zipWhen(super::save).map(Tuple2::getT1));
    }

    public String getRelativeFilePath(Path path) {
        String string = uploadPath.relativize(path).toString();
        return string.replaceAll("\\\\", "/");
    }

    public Mono<SysFile> updateFile(MultipartFile file) throws IOException {
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
}
