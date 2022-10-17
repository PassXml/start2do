package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_role_menu")
@Cache
public class SysRoleMenu extends Model {

    @Id
    @EmbeddedId
    private SysRoleMenuId id;


    @Column(name = "role_id")
    private Integer roleId;
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    @ManyToOne
    private SysRole role;
    @Column(name = "menu_id")
    private Integer menuId;
    @JoinColumn(name = "menu_id", insertable = false, updatable = false)
    @ManyToOne
    private SysMenu menu;

    public SysRoleMenu(SysRoleMenuId id) {
        this.id = id;
    }
}
