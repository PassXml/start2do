package org.start2do.dto.req.position;

import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

@Data
@Accessors(chain = true)
public class PositionPageReq {
    private String name;
    private String code;
    private EnableType status;
}
