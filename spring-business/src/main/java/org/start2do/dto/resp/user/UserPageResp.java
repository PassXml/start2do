package org.start2do.dto.resp.user;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserPageResp {

    private Integer id;
    private String realName;
    private String username;
    private String status;
    private String statusStr;
    private String phone;
    private String email;
    private String avatar;
    private Integer deptId;
    private String deptName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
