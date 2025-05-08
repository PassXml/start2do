package org.start2do.dto.resp.user;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.entity.security.SysRole;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserPageResp {

    private String id;
    private String realName;
    private String username;
    private String status;
    private String statusStr;
    private String phone;
    private String email;
    private String avatar;
    private String deptId;
    private String deptName;
    private List<SysRole> roles;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public List<String> getRoles() {
        return roles.stream().map(SysRole::getName).toList();
    }
}
