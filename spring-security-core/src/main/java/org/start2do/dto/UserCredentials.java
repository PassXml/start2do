package org.start2do.dto;

import java.util.Collection;
import java.util.List;
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

    public UserCredentials(Integer id, String username, String password,
        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}
