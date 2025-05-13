package org.start2do.dto.resp.userauth;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

@Data
@Accessors(chain = true)
public class UserAuthPageResp {
    private String id;
    private String userId;
    private String authType;
    private String authUid;
    private String authUsername;
    private EnableType status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
