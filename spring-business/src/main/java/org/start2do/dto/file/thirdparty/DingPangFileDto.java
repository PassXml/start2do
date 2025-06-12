package org.start2do.dto.file.thirdparty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DingPangFileDto implements Serializable {

    public static String KEY = "DP-V1.0";
    public static String SPACE_ID = "spaceId";
    public static String FILE_ID = "fileId";
    public static String FILE_SIZE = "fileSize";
    public static String FILE_NAME = "fileName";
    public static String PROCESS_INSTANCE_ID = "processInstanceId";
    /**
     * 普通钉盘文件/审批文件
     */
    private String type;
    private String fileId;
    private String spaceId;
    private Integer fileSize;
    private String fileName;
    private String unionId;
    private String uid;
    private String processInstanceId;

    private DingPangFileDto() {
    }

    public DingPangFileDto(String type, String fileId, String spaceId, Integer fileSize, String fileName) {
        this.type = type;
        this.fileId = fileId;
        this.spaceId = spaceId;
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    public static DingPangFileDto buildDD(String fileId, String spaceId, Integer fileSize, String fileName,
        String unionId, String uid) {
        DingPangFileDto dto = new DingPangFileDto(KEY, fileId, spaceId, fileSize, fileName);
        dto.setUnionId(unionId).setUid(uid);
        return dto;
    }

}
