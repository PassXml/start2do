package org.start2do.ebean.entity;

import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@MappedSuperclass
public class BaseModel extends Model {

    /**
     * 创建时间
     */
    @WhenCreated
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @WhoCreated
    @Column(name = "create_person")
    private String createPerson;

    /**
     * 更新时间
     */
    @WhenModified
    @Column(name = "update_time")
    private LocalDateTime updateTime;


    /**
     * 更新人员
     */
    @WhoModified
    @Column(name = "update_person", nullable = true)
    private String updatePerson;

    /**
     * 删除时间
     */
    @Column(name = "delete_time")
    private LocalDateTime deleteTime;

    /**
     * 删除人员
     */
    @Column(name = "delete_person")
    private String deletePerson;

    /**
     * 是否删除
     */
    @SoftDelete
    @Column(name = "is_delete")
    private Boolean isDelete = Boolean.FALSE;
    @Version
    @Column(name = "version")
    private Long version;
}
