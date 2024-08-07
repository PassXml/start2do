package org.start2do.entity.business;

import io.ebean.Model;
import io.ebean.annotation.DbComment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.enums.YesOrNoType;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_remove_file")
public class SysRemoveFile extends Model {

    @Id
    @Column(length = 64)
    private UUID id;
    @DbComment("file_id")
    @Column(length = 64, name = "file_id")
    private UUID fileId;
    private YesOrNoType success;
    private LocalDateTime createTime;

    public SysRemoveFile(UUID fileId) {
        this.id = UUID.randomUUID();
        this.success = YesOrNoType.No;
        this.fileId = fileId;
    }
}
