package org.start2do.entity.security;


import java.io.Serializable;
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
@EqualsAndHashCode
@Embeddable
public class SysUserRoleId implements Serializable {

    private Integer userId;
    private Integer roleId;

}
