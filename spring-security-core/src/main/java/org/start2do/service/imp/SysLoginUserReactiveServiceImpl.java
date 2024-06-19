package org.start2do.service.imp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.UserRole;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.ebean.service.IReactiveService;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUser.Status;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.reactive.ISysLoginUserCustomInfoReactiveService;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SysLoginUserReactiveServiceImpl extends AbsReactiveService<SysUser, Integer> implements
    ReactiveUserDetailsService {

    private final ISysLoginUserCustomInfoReactiveService sysLoginUserCustomInfoService;


    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return findOne(new QSysUser().setUseCache(true).username.eq(username).menus.roles.filterMany(
            new QSysRole().menus.status.eq(EnableType.Enable).getExpressionList()))
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("用户名不存在")))
            .filter(sysUser -> sysUser.getStatus() != Status.Lock)
            .switchIfEmpty(Mono.error(new BusinessException("账户被锁定,不能使用"))).flatMap(
                user -> Mono.deferContextual(
                        contextView -> Mono.just(contextView.getOrEmpty(IReactiveService.TokenKey)))
                    .<UserCredentials>handle((o, sink) -> {
                            List<SysRole> roles = user.getRoles();
                            Set<GrantedAuthority> authorities = new HashSet<>();
                            UserCredentials credentials = new UserCredentials(user.getId(), user.getUsername(),
                                user.getPassword(), user.getRealName(), authorities);
                            credentials.setRoles(roles.stream()
                                .map(sysRole -> new UserRole(sysRole.getId(), sysRole.getName(), sysRole.getRoleCode()))
                                .toList());
                            credentials.setDept(user.getDept());
                            sink.next(credentials);
                    })).zipWhen(userCredentials -> sysLoginUserCustomInfoService.getUserExtInfo(userCredentials.getId()))
            .map(objects -> {
                UserCredentials credentials = objects.getT1();
                credentials.setUserExtInfo(objects.getT2());
                return credentials;
            });


    }
}
