package org.start2do.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;
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
public class SysPermissionUserRefId implements Serializable {

    @Id
    @Column(name = "permission_id")
    private Integer permissionId;
    @Id
    @Column(name = "user_id")
    private Integer userId;

    public SysPermissionUserRefId(Integer permissionId, Integer userId) {
        this.permissionId = permissionId;
        this.userId = userId;
    }
}
