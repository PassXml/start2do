package org.start2do.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

    @Column(name = "permission_id")
    private String permissionId;
    @Column(name = "user_id")
    private String userId;

    public SysPermissionUserRefId(String permissionId, String userId) {
        this.permissionId = permissionId;
        this.userId = userId;
    }
}
