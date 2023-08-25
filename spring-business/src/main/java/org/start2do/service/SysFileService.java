package org.start2do.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.start2do.BusinessConfig;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.business.SysFile;

@Service
@RequiredArgsConstructor
public class SysFileService extends AbsService<SysFile> {

    private final BusinessConfig businessConfig;


    public void removeFileById(String fileId) {
        SysFile file = getById(fileId);
        try {
            Files.delete(Paths.get(businessConfig.getFileSetting().getUploadDir() + file.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
