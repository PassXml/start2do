package org.start2do.entity.security;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
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
public class SysUserPositionEntityId implements Serializable {

    private Integer userId;
    @Column(length = DBConstant.UUID_STR_LENGTH)
    private String positionId;

    public SysUserPositionEntityId(Integer userId, String positionId) {
        this.userId = userId;
        this.positionId = positionId;
    }
}
