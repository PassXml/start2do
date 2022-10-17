package org.start2do.dto.req.role;

import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class RoleAddReq {

    @NotEmpty
    private String name;
    @NotEmpty
    private String roleCode;
    private String descs;
}
