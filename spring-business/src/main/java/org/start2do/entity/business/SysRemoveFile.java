package org.start2do.entity.business;

import io.ebean.Model;
import io.ebean.annotation.DbComment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.ebean.enums.YesOrNoType;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_remove_file")
public class SysRemoveFile extends Model {

    @Id
    @Column(length = DBConstant.ID_STR_LENGTH)
    @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
    private String id;
    @DbComment("file_id")
    @Column(length = 64, name = "file_id")
    private String fileId;
    private YesOrNoType success;
    private LocalDateTime createTime;

    public SysRemoveFile(String fileId) {
        this.success = YesOrNoType.No;
        this.fileId = fileId;
    }
}
