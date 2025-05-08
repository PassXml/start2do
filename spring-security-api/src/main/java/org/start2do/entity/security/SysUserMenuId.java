package org.start2do.entity.security;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class SysUserMenuId implements Serializable {

    @Column(name = "menu_id", length = DBConstant.ID_STR_LENGTH)
    private String menuId;
    @Column(name = "user_id", length = DBConstant.ID_STR_LENGTH)
    private String userId;

}
