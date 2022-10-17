package org.start2do.ebean.entity;

import io.ebean.Model;
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
import org.start2do.util.ExcelUtil.ExcelSetting;

@Setter
@Getter
@Accessors(chain = true)
@MappedSuperclass
public class BaseModel2 extends Model {

    /**
     * 创建时间
     */
    @WhenCreated
    @ExcelSetting("创建时间")
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @WhoCreated
    @ExcelSetting("创建人")
    @Column(name = "create_person")
    private String createPerson;

    /**
     * 更新时间
     */
    @WhenModified
    @ExcelSetting("更新时间")
    @Column(name = "update_time")
    private LocalDateTime updateTime;


    /**
     * 更新人员
     */
    @WhoModified
    @ExcelSetting("更新人")
    @Column(name = "update_person", nullable = true)
    private String updatePerson;

    @Version
    @ExcelSetting("版本号")
    @Column(name = "version")
    private Long version;
}
