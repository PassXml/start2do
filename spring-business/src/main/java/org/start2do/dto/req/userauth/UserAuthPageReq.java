package org.start2do.dto.req.userauth;

import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

@Data
@Accessors(chain = true)
public class UserAuthPageReq {
    private int current = 1; // 当前页码，默认为1
    private int size = 10;   // 每页数量，默认为10

    // 过滤参数
    private String userId;
    private String authType;
    private String authUsername;
    private String authUid;
    private EnableType status;
}
