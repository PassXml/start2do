package org.start2do.dto.req.log;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SysLoginLogReq extends Page {

    private String username;
    private String ip;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
