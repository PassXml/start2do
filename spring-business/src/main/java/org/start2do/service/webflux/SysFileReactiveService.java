package org.start2do.service.webflux;

import jakarta.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.dto.dto.file.FileUpdateByteDto;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.business.SysFile;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysFileReactiveService extends AbsReactiveService<SysFile, Integer> implements CommandLineRunner {

    private final BusinessConfig businessConfig;
    @Lazy
    @Resource
    private IFileOperationService operationService;

    public Mono<Boolean> removeFileById(String fileId) {
        return operationService.remove(fileId);
    }


    public Mono<Boolean> download(ServerHttpResponse response, String fileId) {
        return operationService.download(response, fileId);
    }

    public Mono<List<SysFile>> uploadFile(Boolean checkExist, FileUpdateByteDto... dtos) {
        List<Mono<SysFile>> result = new ArrayList<>();
        for (FileUpdateByteDto dto : dtos) {
            result.add(operationService.update(dto.getBytes(), dto.getFileName(), checkExist));
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


    public Mono<List<SysFile>> uploadFile(Boolean checkExist, FilePart... file) {
        List<Mono<SysFile>> result = new ArrayList<>();
        for (FilePart part : file) {
            result.add(operationService.update(part, checkExist));
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


    @Override
    public void run(String... args) throws Exception {
        Path path = Paths.get(businessConfig.getFileSetting().getUploadDir());
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
