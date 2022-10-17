package org.start2do.dto.resp.role;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class RoleDetailResp {

    private Integer id;
    private String name;
    private String roleCode;
    private String descs;
    private String createPerson;
    private String updatePerson;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
