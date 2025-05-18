package org.start2do.dto.req.permission;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PermissionRoleAddReq {
  private List<String> roleId;
  private List<String> permissionId;
}
