package org.start2do.dto.req.role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class RolePageReq {

    private String roleName;
    private String roleCode;
}
