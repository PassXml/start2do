package org.start2do.service.imp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.UserRole;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUser.Status;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.SysLoginUserService;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
public class SysLoginUserServiceImpl extends AbsService<SysUser> implements SysLoginUserService<SysUser>, UserDetailsService {


    @Override
    public UserCredentials loadUserByUsername(String username) throws UsernameNotFoundException {
        QSysUser sysUser = new QSysUser().menus.fetch().roles.menus.fetch().roles.fetch();
        SysUser user = findOne(sysUser.username.eq(username));
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        if (user.getStatus() == Status.Lock) {
            throw new BusinessException("账户被锁定,不能使用");
        }
        List<SysRole> roles = user.getRoles();
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        UserCredentials credentials = new UserCredentials(user.getId(), user.getUsername(), user.getPassword(),
            authorities);
        credentials.setRoles(roles.stream()
            .map(sysRole -> new UserRole(sysRole.getId(), sysRole.getName(), sysRole.getRoleCode()))
            .collect(Collectors.toList()));
        credentials.setDept(user.getDept());
        return credentials;
    }
}
