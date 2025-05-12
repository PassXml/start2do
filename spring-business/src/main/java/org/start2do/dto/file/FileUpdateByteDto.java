package org.start2do.dto.file;

import java.io.IOException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.dto.BusinessException;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class FileUpdateByteDto {

    private String fileName;
    private byte[] bytes;

    public FileUpdateByteDto(MultipartFile file) throws IOException {
        this.fileName = file.getOriginalFilename();
        this.bytes = file.getBytes();
        if (ArrayUtils.isEmpty(this.bytes)) {
            throw new BusinessException("不能上传空文件");
        }
    }
    public FileUpdateByteDto(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
        if (ArrayUtils.isEmpty(this.bytes)) {
            throw new BusinessException("不能上传空文件");
        }
    }
}
