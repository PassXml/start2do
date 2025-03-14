package org.start2do.ops.dto.deploy;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DeployReq {

    @NotEmpty
    private String destPath;
    @NotNull
    private MultipartFile file;
    private String rootFileName;
}
