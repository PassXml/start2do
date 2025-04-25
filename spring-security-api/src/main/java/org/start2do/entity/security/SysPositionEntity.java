package org.start2do.entity.security;

import io.ebean.annotation.DbComment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;

@Setter
@Getter
@Entity
@NoArgsConstructor
@DbComment("岗位表")
@Accessors(chain = true)
@Table(name = "sys_position")
public class SysPositionEntity extends BaseModel2 implements Serializable {

    @Id
    @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
    @Column(length = DBConstant.UUID_STR_LENGTH)
    private String id;
    @DbComment("上级")
    @Column(name = "parent_id", length = DBConstant.UUID_STR_LENGTH)
    private String parentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private SysPositionEntity parent;
    @DbComment("名称")
    @Column(length = DBConstant.TITLE_LENGTH)
    private String name;
    @DbComment("编码")
    @Column(length = DBConstant.REALNAME_LENGTH)
    private String code;
    @DbComment("是否启用")
    private EnableType status;
    @Column
    private Integer sort;

    @DbComment("岗位来源类型：local-本地，third-第三方")
    @Column(length = 32)
    private String sourceType;

    @DbComment("第三方岗位唯一标识")
    @Column(length = 64)
    private String sourceId;
}
