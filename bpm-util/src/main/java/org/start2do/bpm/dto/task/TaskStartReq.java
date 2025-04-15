package org.start2do.bpm.dto.task;

import java.util.Map;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class TaskStartReq {
    @NotEmpty
    private String businessId;
    @NotEmpty
    private String flowCode;

    private Map<String, Object> variable;
}
