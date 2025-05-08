package org.start2do.dto.resp.dept;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DeptPageResp {

    private String id;
    private String name;
    private Integer sort = 0;
    private String parentId;
    private String deptCode;
    private LocalDateTime createTime;
}
