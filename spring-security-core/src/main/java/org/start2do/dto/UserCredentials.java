package org.start2do.dto;

import java.awt.Menu;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.start2do.entity.security.SysDept;

@Setter
@Getter
@Accessors(chain = true)
public class UserCredentials extends User {

    private Integer id;
    private List<UserRole> roles;
    private List<Menu> menus;
    private SysDept dept;

    public UserCredentials(Integer id, String username, String password,
        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}
