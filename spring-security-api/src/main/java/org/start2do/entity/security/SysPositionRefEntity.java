package org.start2do.entity.security;

import io.ebean.Model;
import io.ebean.annotation.DbComment;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;

@Setter
@Getter
@Entity
@NoArgsConstructor
@DbComment("岗位表用户关联表")
@Accessors(chain = true)
@Table(name = "sys_position_user")
public class SysPositionRefEntity extends Model implements Serializable {

    @Id
    @EmbeddedId
    private SysPositionRefId id;
    @Column(length = DBConstant.ID_STR_LENGTH)
    private String userId;


    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysUser user;
    @Column(length = DBConstant.ID_STR_LENGTH)
    private String postId;

    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysPositionEntity position;

    public SysPositionRefEntity(SysPositionRefId id) {
        this.id = id;
    }
}
