package org.start2do.service.webflux;


import java.nio.file.Path;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.start2do.entity.business.SysFile;
import reactor.core.publisher.Mono;

public interface IFileOperationService {



    Mono remove(String fileId);

    Mono<SysFile> update(FilePart part, Boolean checkExist);

    Mono<Boolean> download(ServerHttpResponse response, String fileId);

    default String getSubFix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    default String getRelativeFilePath(Path basePath, Path path) {
        String string = basePath.relativize(path).toString();
        return string.replaceAll("\\\\", "/");
    }

    default <R> Mono<DataBuffer> fileToBytes(FilePart part) {
        return DataBufferUtils.join(part.content());
    }

}
