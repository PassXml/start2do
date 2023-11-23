package org.start2do.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.SysMenu;

@Setter
@Getter
@Accessors(chain = true)
public class UserCredentials extends User {

    private Integer id;
    private List<UserRole> roles;
    private List<SysMenu> menus;
    private SysDept dept;
    private String realName;
    private Map<String, Object> customInfo;

    public UserCredentials(Integer id, String username, String password, String realName,
        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.realName = realName;
    }

}
