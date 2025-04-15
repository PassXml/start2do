package org.start2do.bpm.dto.flow;

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
public class FlowDeployReq {

    @NotNull
    private Type type = Type.JSON;
    private String json;
    private MultipartFile file;

    public enum Type {
        JSON, JSONFILE
    }


}
