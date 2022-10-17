package org.start2do.entity.security;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
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
    private Integer roleId;
    @Column(name = "menu_id")
    private Integer menuId;

    public SysRoleMenuId(Integer roleId, Integer menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
