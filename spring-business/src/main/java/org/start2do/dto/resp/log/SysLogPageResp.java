package org.start2do.dto.resp.log;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SysLogPageResp {

    private String id;
    private String username;
    private String userAgent;
    private String ip;
    private LocalDateTime createTime;

}
