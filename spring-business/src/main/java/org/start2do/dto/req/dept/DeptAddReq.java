package org.start2do.dto.req.dept;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DeptAddReq {

    @NotEmpty
    private String name;
    private Integer sort;
    private Integer parentId;

}
