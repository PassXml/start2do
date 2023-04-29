package org.start2do.dto.resp.login;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.UserRole;
import org.start2do.entity.security.SysDept;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class JwtResponse {

    /** H
     * 用户Id
     */
    private Integer id;
    /**
     * 用户组
     */
    private List<UserRole> roles;
    /**
     * 用户名
     */
    private String username;
    private String realName;
    private Integer deptId;
    private String deptName;
    /**
     * jwt Token
     */
    private String jwt;

    public JwtResponse(UserCredentials userCredentials, String jwt) {
        this.id = userCredentials.getId();
        this.username = userCredentials.getUsername();
        this.realName= userCredentials.getRealName();
        this.jwt = jwt;
        this.roles = userCredentials.getRoles();
        Optional<SysDept> optional = Optional.ofNullable(userCredentials.getDept());
        this.deptId = optional.map(SysDept::getId).orElse(null);
        this.deptName = optional.map(SysDept::getName).orElse(null);
    }
}
