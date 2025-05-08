package org.start2do.dto.req.permission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PermissionRoleAddReq {
    @NotNull
    private String roleId;
    @NotEmpty
    private String permissionId;
}
