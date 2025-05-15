package org.start2do.dto.permission;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class PermissionAssignRequest {
    private String permissionId;
    private List<String> userIds;
    private List<String> roleIds;
}
