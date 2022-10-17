package org.start2do.entity.security;

import io.ebean.annotation.Cache;
import io.ebean.annotation.Identity;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_dept")
@Cache
public class SysDept extends BaseModel2 implements Serializable {

    @Id
    @Identity(start = 10)
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", length = 128)
    private String name;
    @Column(name = "sort")
    private Integer sort = 0;
    @Column(name = "parent_id")
    private Integer parentId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private SysDept parent;

}
