package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Entity
@Table(name = "sys_user_role")
@NoArgsConstructor
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysUserRole extends Model {

    @Id
    @EmbeddedId
    private SysUserRoleId id;

    @Column(name = "user_id")
    private Integer userId;
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysUser user;
    @Column(name = "role_id")
    private Integer roleId;

    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysRole role;

    public SysUserRole(Integer userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
