package org.start2do.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.service.IFileFilter;
import reactor.core.publisher.Mono;

/**
 * @author Lijie
 */
@Service
@ConditionalOnMissingBean(IFileFilter.class)
public class FileFilterEmptyImpl implements IFileFilter {

    @Override
    public void filter(MultipartFile file) {

    }

    @Override
    public Mono<FilePart> filter(FilePart file) {
        return Mono.justOrEmpty(file);
    }
}
