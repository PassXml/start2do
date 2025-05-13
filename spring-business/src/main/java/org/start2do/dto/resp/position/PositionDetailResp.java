package org.start2do.dto.resp.position;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

@Data
@Accessors(chain = true)
public class PositionDetailResp {
    private String id;
    private String parentId;
    private String name;
    private String code;
    private EnableType status;
    private Integer sort;
    private String sourceType;
    private String sourceId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
