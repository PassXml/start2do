package org.start2do.dto.req.permission;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PermissionRoleAddReq {

    @Size(max = 999)
    private List<String> roleId;
    private String groupName;
    private List<String> permissionId;
}
