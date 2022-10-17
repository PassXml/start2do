package org.start2do.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserRole {

    private Integer roleId;
    private String roleName;
    private String roleCode;

    public UserRole(Integer roleId, String roleName, String roleCode) {

        this.roleId = roleId;
        this.roleName = roleName;
        this.roleCode = roleCode;
    }
}
