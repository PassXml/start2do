package org.start2do.entity.security;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class SysRoleMenuId implements Serializable {

    @Column(name = "role_id")
    private String roleId;
    @Column(name = "menu_id")
    private String menuId;

    public SysRoleMenuId(String roleId, String menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
