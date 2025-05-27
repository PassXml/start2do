package org.start2do.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.Model;
import io.ebean.annotation.DbComment;
import io.ebean.annotation.Index;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.ebean.enums.YesOrNoType;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@DbComment("文件上传任务(用于同步第三方)")
@Table(name = "sys_file_task")
public class SysFileTask extends Model {

    @Id
    @MapsId
    @Column(name = "file_id", length = DBConstant.ID_STR_LENGTH)
    private String fileId;
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysFile file;
    @Index
    @Column(name = "completed")
    private YesOrNoType completed = YesOrNoType.No;

    @Lob
    private String errorMsg;
    @WhenCreated
    private LocalDateTime creatTime;

    @WhenModified
    private LocalDateTime updateTime;

    public SysFileTask(String fileId) {
        this.fileId = fileId;
    }
}
