package org.start2do.bpm.dto.task;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    private String businessId;
    @NotEmpty
    private String flowCode;

    @JsonAlias("vars")
    private Map<String, Object> variable;
}
