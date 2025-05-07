package org.start2do.dto.req.permission;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class PermissionUserUpdateReq {
    private Integer userId;
    private List<Integer> permissionIds;
}
