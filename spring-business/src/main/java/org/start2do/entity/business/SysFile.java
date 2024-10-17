package org.start2do.entity.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_file")
public class SysFile extends BaseModel2 {

    @Id
    @Column(length = DBConstant.ID_STR_LENGHT)
    @GeneratedValue(generator = SnowflakeStrGenerator.KEY)
    private String id;
    @Column(length = 1024)
    private String fileName;
    /**
     * 路径
     */
    @Column(length = 1024)
    private String filePath;
    /**
     * 路径
     */
    @Column(length = 1024)
    private String relativeFilePath;

    @Column(length = 32)
    private String fileMd5;
    @Column(length = 128)
    private String host;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 后缀
     */
    private String suffix;

    public SysFile(String fileName, String filePath, String relativeFilePath, String fileMd5, String host,
        Long fileSize, String suffix) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileMd5 = fileMd5;
        this.relativeFilePath = relativeFilePath;
        this.host = host;
        this.fileSize = fileSize;
        this.suffix = suffix;
    }

    public String getUrl() {
        if (relativeFilePath.startsWith("/")) {
            return host + relativeFilePath;
        }
        return host + "/" + relativeFilePath;
    }
}
