package org.start2do.service.imp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.UserRole;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.service.AbsService;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUser.Status;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.ISysLoginUserCustomInfoService;
import org.start2do.service.SysLoginUserService;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SysLoginUserServiceImpl extends AbsService<SysUser> implements SysLoginUserService<SysUser>,
    UserDetailsService, ReactiveUserDetailsService {

    private final ISysLoginUserCustomInfoService sysLoginUserCustomInfoService;

    @Override
    public UserCredentials loadUserByUsername(String username) throws UsernameNotFoundException {
        QSysUser qClass = new QSysUser().setUseCache(true).menus.roles.filterMany(
            new QSysRole().menus.status.eq(EnableType.Enable).getExpressionList()
        );
        SysUser user = findOne(qClass.username.eq(username));
        if (user == null) {
            ReactiveSecurityContextHolder.clearContext();
            SecurityContextHolder.clearContext();
            throw new UsernameNotFoundException("用户名不存在");
        }
        if (user.getStatus() == Status.Lock) {
            ReactiveSecurityContextHolder.clearContext();
            SecurityContextHolder.clearContext();
            throw new BusinessException("账户被锁定,不能使用");
        }
        List<SysRole> roles = user.getRoles();
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        UserCredentials credentials = new UserCredentials(user.getId(), user.getUsername(), user.getPassword(),
            user.getRealName(), authorities);
        credentials.setCustomInfo(sysLoginUserCustomInfoService.getCustomInfo(user.getId()));
        credentials.setRoles(
            roles.stream().map(sysRole -> new UserRole(sysRole.getId(), sysRole.getName(), sysRole.getRoleCode()))
                .collect(Collectors.toList()));
        credentials.setDept(user.getDept());
        return credentials;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.justOrEmpty(loadUserByUsername(username));
    }
}
