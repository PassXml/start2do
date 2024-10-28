package org.start2do.dto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultFilePart implements FilePart {

    private File file;
    private DefaultDataBufferFactory defaultDataBufferFactory;

    public DefaultFilePart(File file, DefaultDataBufferFactory defaultDataBufferFactory) {
        this.file = file;
        this.defaultDataBufferFactory = defaultDataBufferFactory;
    }

    public DefaultFilePart(File file) {
        this.file = file;
        this.defaultDataBufferFactory = new DefaultDataBufferFactory();
    }

    @Override
    public String filename() {
        return file.getName();
    }

    @Override
    public Mono<Void> transferTo(Path dest) {
        return Mono.fromRunnable(() -> {
            try {
                Files.copy(file.toPath(), dest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public HttpHeaders headers() {
        return HttpHeaders.EMPTY;
    }

    @Override
    public Flux<DataBuffer> content() {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            DataBuffer dataBuffer = defaultDataBufferFactory.wrap(fileContent);
            return Flux.just(dataBuffer);
        } catch (IOException e) {
            return Flux.error(e);
        }
    }
}
