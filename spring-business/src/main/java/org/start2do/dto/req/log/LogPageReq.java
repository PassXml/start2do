package org.start2do.dto.req.log;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class LogPageReq {

    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime[] timeRange;

}
