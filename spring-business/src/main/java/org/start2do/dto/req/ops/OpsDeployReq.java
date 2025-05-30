package org.start2do.dto.req.ops;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpsDeployReq {

    @NotEmpty
    private String destPath;
    @NotNull
    @JsonIgnore
    private MultipartFile file;
    private boolean clear = false;
    private String rootFileName = "index.html";
    private boolean unzip = false;
    private String totpCode;
}
