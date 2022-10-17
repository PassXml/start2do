package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Entity
@Table(name = "sys_user_role")
@NoArgsConstructor
@Cache
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
