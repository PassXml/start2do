package org.start2do.dto.req.permission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class PermissionUserAddReq {

    @NotNull
    private String userId;
    @NotEmpty
    private String permissionId;
}
