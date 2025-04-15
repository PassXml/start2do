package org.start2do.service;


import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.start2do.entity.business.SysFile;
import reactor.core.publisher.Mono;

public interface IFileOperationService {


    default Mono<Boolean> removeReactive(String fileId) {
        throw new RuntimeException("not support");
    }

    default boolean remove(String fileId) {
        throw new RuntimeException("not support");
    }

    default Mono<SysFile> uploadReactive(FilePart part, Boolean checkExist) {
        throw new RuntimeException("not support");
    }

    default Mono<SysFile> uploadReactive(byte[] bytes, String fileName, Boolean checkExist) {
        throw new RuntimeException("not support");
    }

    default SysFile upload(byte[] bytes, String fileName, Boolean checkExist) {
        throw new RuntimeException("not support");
    }

    default Mono<Boolean> downloadReactive(ServerHttpResponse response, String fileId) {
        throw new RuntimeException("not support");
    }

    default boolean download(HttpServletResponse response, String fileId) {
        throw new RuntimeException("not support");
    }

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
