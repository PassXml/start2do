package org.start2do.dto.req.dept;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DeptUpdateReq extends DeptAddReq {

    @NotNull
    private Integer id;
}
