package org.start2do.entity.security;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_user_position")
public class SysUserPositionEntity extends BaseModel2 {

    @EmbeddedId
    private SysUserPositionEntityId id;

    private Integer userId;
    @Column(length = DBConstant.UUID_STR_LENGTH)
    private String positionId;

    public SysUserPositionEntity(SysUserPositionEntityId id) {
        this.id = id;
    }

    public SysUserPositionEntity(Integer userId, String positionId) {
        this.userId = userId;
        this.positionId = positionId;
        this.id = new SysUserPositionEntityId(userId, positionId);
    }
}
