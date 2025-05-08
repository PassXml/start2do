package org.start2do.dto.req.permission;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
public class PermissionRolePageReq extends Page {

    @NotNull
    private String roleId;
}
