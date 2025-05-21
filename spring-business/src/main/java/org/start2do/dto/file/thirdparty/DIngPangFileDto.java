package org.start2do.dto.file.thirdparty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DIngPangFileDto implements Serializable {

    public static String KEY = "DP-V1.0";
    /**
     * 普通钉盘文件/审批文件
     */
    private String type;
    private String fileId;
    private String spaceId;
    private Integer fileSize;
    private String processInstanceId;
}
