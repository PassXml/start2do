package org.start2do.service.webflux;

import io.ebean.DB;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsMixService;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.service.reactive.SysLoginRoleReactiveService;
import org.start2do.util.ListUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "user", havingValue = "true")
public class SysUserReactiveService extends AbsMixService<SysUser, Integer> {

    private final SysLoginRoleReactiveService sysLoginRoleReactiveService;
    private final SysUserRoleReactiveService sysUserRoleService;
    private final PasswordEncoder passwordEncoder;

    public Mono<Boolean> add(SysUser entity, List<Integer> roles) {
        return transactionOf(checkRole(roles).zipWith(Mono.just(entity)).zipWhen(objs -> {
                SysUser user = objs.getT2();
                user.setPassword(passwordEncoder.encode(entity.getPassword()));
                return saveReactive(user);
            }).flatMap(objects -> {
                SysUser user = objects.getT1().getT2();
                List<Mono<Boolean>> monos = new ArrayList<>();
                for (Integer roleId : roles) {
                    monos.add(sysUserRoleService.saveReactive(new SysUserRole(user.getId(), roleId)).map(sysUserRole -> true));
                }
                return Flux.fromIterable(monos).flatMap(Function.identity()).all(Boolean::booleanValue);
            }).filter(aBoolean -> aBoolean).switchIfEmpty(Mono.error(new BusinessException("保存用户失败"))),
            DB.beginTransaction());

    }


    private Mono<List<SysRole>> checkRole(List<Integer> roles) {
        return sysLoginRoleReactiveService.findAll(new QSysRole().id.in(roles))
            .filter(sysRoles -> sysRoles.size() == roles.size())
            .switchIfEmpty(Mono.error(new BusinessException("用户组错误")));
    }

    public Mono<Boolean> remove(Integer id) {
        return sysUserRoleService.deleteReactive(new QSysUserRole().userId.eq(id)).filter(aBoolean -> true)
            .switchIfEmpty(Mono.error(new BusinessException("删除失败"))).flatMap(aBoolean -> deleteByIdReactive(id));
    }

    public Mono<Boolean> update(SysUser user, List<Integer> roleIds) {
        return transactionOf(checkRole(roleIds).zipWhen(sysRoles -> this.updateReactive(user))
                .zipWhen(aBoolean -> sysUserRoleService.findAllReactive(new QSysUserRole().userId.eq(user.getId())))
                .flatMap(objects -> {
                    List<SysUserRole> userRoles = objects.getT2();
                    List<SysRole> roles = objects.getT1().getT1();
                    List<Mono<Boolean>> result = new ArrayList<>();
                    ListUtil.diff(roles, userRoles, (roleEntity, sysRole) -> sysRole.getRoleId().equals(roleEntity.getId()),
                        roleItem -> {
                            for (SysRole item : roleItem) {
                                result.add(sysUserRoleService.saveReactive(new SysUserRole(user.getId(), item.getId()))
                                    .map(sysUserRole -> true));
                            }
                        }, null, sysUserRoles -> {
                            result.add(sysUserRoleService.deleteReactive(new QSysUserRole().userId.eq(user.getId()).roleId.in(
                                sysUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet()))));
                        });
                    return Flux.fromIterable(result).flatMap(Function.identity()).all(Boolean::booleanValue);
                }).filter(aBoolean -> aBoolean).switchIfEmpty(Mono.error(new BusinessException("更新失败"))),
            DB.beginTransaction());

    }

    public Mono<Integer> checkUserName(String username) {
        return countReactive(new QSysUser().username.eq(username)).filter(integer -> integer <= 0)
            .switchIfEmpty(Mono.error(new BusinessException("用户名已被使用")));
    }
}
