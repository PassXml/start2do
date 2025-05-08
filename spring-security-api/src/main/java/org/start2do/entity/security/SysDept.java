package org.start2do.entity.security;

import io.ebean.annotation.Cache;
import io.ebean.annotation.JsonIgnore;
import io.ebean.annotation.StorageEngine;
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
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_dept")
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysDept extends BaseModel2 implements Serializable {

    @Id
    @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
    private String id;
    @Column(name = "name", length = 128)
    private String name;
    @Column(name = "sort")
    private Integer sort = 0;
    @Column(name = "parent_id")
    private String parentId;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private SysDept parent;
    @Column(length = DBConstant.ID_STR_LENGTH)
    private String deptCode;

}
