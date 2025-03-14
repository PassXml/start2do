package org.start2do.ops.service;

import java.io.InputStream;
import java.nio.file.Paths;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.start2do.util.ZipUtil;

@Service
@RequiredArgsConstructor
public class DeployService {

    public void deploy(@NotEmpty String filePath, InputStream inputStream, String rootFileName) {
        ZipUtil.unzip(inputStream, Paths.get(filePath), rootFileName);
    }
}
