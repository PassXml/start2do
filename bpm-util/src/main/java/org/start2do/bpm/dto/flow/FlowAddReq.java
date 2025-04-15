package org.start2do.bpm.dto.flow;

import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class FlowAddReq {

    @NotEmpty
    private String flowCode;
    @NotEmpty
    private String flowName;
    private String category;
    private String formCustom;
    private String formPath;

}
