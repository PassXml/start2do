package org.start2do.entity.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.ebean.annotation.DbMap;
import io.ebean.annotation.Index;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.ebean.entity.BaseModel;
import org.start2do.ebean.id_generators.SnowflakeStrGenerator;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "sys_file_ext")
public class SysFileExtInfo extends BaseModel {

    @Id
    @MapsId
    @Column(name = "file_id", length = DBConstant.ID_STR_LENGTH)
    private String fileId;
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SysFile file;
    private String type;
    @Lob
    @DbMap
    private Map<String, String> extInfo;
}
