package org.start2do.dto.req.permission;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class PermissionRoleUpdateReq {
    private Integer roleId;
    private List<String> permissionIds;
}
