package org.start2do.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class SysPermissionRoleRefId implements Serializable {

    @Column(name = "permission_id")
    private String permissionId;
    @Column(name = "role_id")
    private String roleId;

    public SysPermissionRoleRefId(String permissionId, String roleId) {
        this.permissionId = permissionId;
        this.roleId = roleId;
    }
}
