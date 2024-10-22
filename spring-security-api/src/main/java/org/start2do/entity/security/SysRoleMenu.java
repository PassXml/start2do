package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
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
