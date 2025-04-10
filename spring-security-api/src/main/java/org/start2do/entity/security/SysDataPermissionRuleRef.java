package org.start2do.entity.security;


import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.entity.security.SysDataPermissionRuleRefId.Type;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_permission_rule_ref")
public class SysDataPermissionRuleRef implements Serializable {

    @EmbeddedId
    private SysDataPermissionRuleRefId id;

    @Column(length = DBConstant.UUID_STR_LENGTH)
    private String ruleId;
    private Type sourceType;
    @Column(length = DBConstant.UUID_STR_LENGTH)
    private String sourceId;
}
