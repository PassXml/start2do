package org.start2do.entity.security;

import io.ebean.annotation.DbComment;
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
@DbComment("用户个性化权限关联表")
@Table(name = "sys_permission_user_ref")
public class SysPermissionUserRef {

    @EmbeddedId
    private SysPermissionUserRefId id;
    @Column(name = "permission_id")
    private Integer permissionId;
    @Column(name = "user_id")
    private Integer userId;

    public SysPermissionUserRef(SysPermissionUserRefId id) {
        this.id = id;
    }
}
