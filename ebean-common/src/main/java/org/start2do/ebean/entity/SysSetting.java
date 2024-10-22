package org.start2do.ebean.entity;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.Identity;
import io.ebean.annotation.IdentityGenerated;
import io.ebean.annotation.IdentityType;
import io.ebean.annotation.Index;
import io.ebean.annotation.StorageEngine;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

/**
 * 设置表
 */
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_setting")
@DbComment("字典值表")
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysSetting extends BaseModel2 implements Serializable {

    @Id
    @Identity(type = IdentityType.IDENTITY, generated = IdentityGenerated.BY_DEFAULT)
    private Long id;

    @Column(name = "enable", length = 2)
    @Index
    private EnableType enable;

    @Column(name = "remark", length = 512)
    private String remark;
    @Index
    @Column(name = "type", length = 128)
    private String type;
    @Column(name = "kkey", length = 128)
    @Index
    private String key;
    @Column(name = "vvalue", length = 2048)
    private String value;
    @Column(name = "sort")
    private Integer sort;

    private static final long serialVersionUID = 1L;

    public SysSetting(EnableType enable, String remark, String type, String key, String value) {
        this.enable = enable;
        this.remark = remark;
        this.type = type;
        this.key = key;
        this.value = value;
        this.sort = 0;
    }
}
