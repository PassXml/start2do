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
import org.start2do.entity.security.SysUrlPermission.SourceType;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class SysUrlPermissionId implements Serializable {

    @Column(length = DBConstant.URL_LENGTH)
    private String uri;
    private SourceType sourceType;
    @Column(length = DBConstant.ADDRESS_LENGTH)
    private String sourceId;

    public SysUrlPermissionId(String uri, SourceType sourceType, String sourceId) {
        this.uri = uri;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }
}
