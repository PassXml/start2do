package org.start2do.entity.security;

import io.ebean.annotation.Cache;
import io.ebean.annotation.Identity;
import io.ebean.annotation.IdentityType;
import io.ebean.annotation.StorageEngine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_dept")
@Cache(enableQueryCache = true)
@StorageEngine("ENGINE = MergeTree() order by id;")
public class SysDept extends BaseModel2 implements Serializable {

    @Id
    @Identity(start = 10, sequenceName = "sys_dept", type = IdentityType.IDENTITY)
    private Integer id;
    @Column(name = "name", length = 128)
    private String name;
    @Column(name = "sort")
    private Integer sort = 0;
    @Column(name = "parent_id")
    private Integer parentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private SysDept parent;

}
