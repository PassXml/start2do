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
    private Integer permissionId;
    @Column(name = "role_id")
    private Integer roleId;

    public SysPermissionRoleRefId(Integer permissionId, Integer roleId) {
        this.permissionId = permissionId;
        this.roleId = roleId;
    }
}
