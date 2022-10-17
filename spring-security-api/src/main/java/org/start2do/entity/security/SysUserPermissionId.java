package org.start2do.entity.security;

import java.io.Serializable;
import javax.persistence.Column;
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
@Embeddable
@EqualsAndHashCode
public class SysUserPermissionId implements Serializable {

    @Column(name = "menu_id")
    private Integer menuId;
    @Column(name = "user_id")
    private Integer userId;

}
