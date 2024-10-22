package org.start2do.entity.security;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@DbComment("权限角色关联表")
@Table(name = "sys_permission_role_ref")
@StorageEngine("ENGINE = MergeTree() order by id;")
@Cache(enableQueryCache = true)
public class SysPermissionRoleRef {

    @EmbeddedId
    private SysPermissionRoleRefId id;
    @Column(name = "permission_id")
    private Integer permissionId;
    @Column(name = "role_id")
    private Integer roleId;

    public SysPermissionRoleRef(SysPermissionRoleRefId id) {
        this.id = id;
    }
}
