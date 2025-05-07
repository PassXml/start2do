package org.start2do.dto.resp.permission;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import org.start2do.entity.security.SysPermission;

@Setter
@Getter
public class PermissionUserPageResp {

    private Integer userId;
    private String username;
    private List<PermissionPageResp> permissions;

    public PermissionUserPageResp(SysPermission permission) {
    }
}
