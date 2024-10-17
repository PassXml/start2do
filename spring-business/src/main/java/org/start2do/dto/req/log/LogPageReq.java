package org.start2do.dto.req.log;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDateTime;
import java.util.List;
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

    @JsonAlias("logDesc")
    private String keyword;
    private List<Long> ids;
}
