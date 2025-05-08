package org.start2do.entity.security;


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
public class SysUserDeptId implements Serializable {

    private String userId;
    private String deptId;

    public SysUserDeptId(String userId, String deptId) {
        this.userId = userId;
        this.deptId = deptId;
    }
}
