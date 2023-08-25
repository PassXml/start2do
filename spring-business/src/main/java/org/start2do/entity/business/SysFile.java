package org.start2do.entity.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.entity.BaseModel2;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "t_sys_file")
public class SysFile extends BaseModel2 {

    @Id
    private java.util.UUID id;
    @Column(length = 1024)
    private String fileName;
    private String filePath;
    @Column(length = 32)
    private String fileMd5;
    private String host;
    private Long fileSize;
    private String suffix;

    public SysFile(String fileName, String filePath, String fileMd5, String host, Long fileSize) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileMd5 = fileMd5;
        this.host = host;
        this.fileSize = fileSize;
        this.suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
