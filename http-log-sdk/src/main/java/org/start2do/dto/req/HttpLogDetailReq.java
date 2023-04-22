package org.start2do.dto.req;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class HttpLogDetailReq {

    @NotEmpty
    private String routeId;
    @NotNull
    private Long timestamp;
}
