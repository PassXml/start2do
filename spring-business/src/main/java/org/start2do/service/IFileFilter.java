package org.start2do.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface IFileFilter {

    void filter(MultipartFile file);

    Mono<FilePart> filter(FilePart file);
}
