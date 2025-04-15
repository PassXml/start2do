package org.start2do.dto.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class FileUpdateResultDto {

    private String fileName;
    private String fullPath;
    private String suffix;
    private String relativePath;
    private String md5;
    private long size = 0;


    public FileUpdateResultDto(String fileName, String fullPath, String suffix, String relativePath, String md5,
        long size) {
        this.fileName = fileName;
        this.fullPath = fullPath;
        this.suffix = suffix;
        this.relativePath = relativePath;
        this.md5 = md5;
        this.size = size;
    }
}
