package org.start2do.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.start2do.constant.DBConstant;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class SysPositionRefId implements Serializable {

    @Column(length = DBConstant.ID_STR_LENGTH)
    private String userId;
    @Column(length = DBConstant.ID_STR_LENGTH)
    private String postId;

    public SysPositionRefId(String userId, String postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
