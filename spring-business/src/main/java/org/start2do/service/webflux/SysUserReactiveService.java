package org.start2do.service.webflux;

import io.ebean.DB;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.start2do.entity.security.SysUserDept;
import org.start2do.entity.security.SysUserDeptId;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.entity.security.query.QSysUserDept;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.service.reactive.SysLoginRoleReactiveService;
import org.start2do.service.reactive.SysUserDeptReactiveService;
import org.start2do.util.ListUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.business.service", name = "user", havingValue = "true",matchIfMissing = true)
public class SysUserReactiveService extends AbsMixService<SysUser, String> {

    private final SysLoginRoleReactiveService sysLoginRoleReactiveService;
    private final SysUserRoleReactiveService sysUserRoleService;
    private final PasswordEncoder passwordEncoder;
    private final SysUserDeptReactiveService sysUserDeptReactiveService;

    public Mono<Boolean> add(SysUser entity, String mainDept, List<String> roles) {
        return transactionOf(checkRole(roles).zipWith(Mono.just(entity)).zipWhen(objs -> {
                SysUser user = objs.getT2();
                user.setPassword(passwordEncoder.encode(entity.getPassword()));
                return saveReactive(user);
            }).flatMap(objects -> {
                SysUser savedUser = objects.getT2();
                List<Mono<Boolean>> monos = new ArrayList<>();
                for (String roleId : roles) {
                    monos.add(sysUserRoleService.saveReactive(new SysUserRole(savedUser.getId(), roleId)).map(sysUserRole -> true));
                }
                monos.add(sysUserDeptReactiveService.saveReactive(
                    new SysUserDept(new SysUserDeptId(savedUser.getId(), mainDept), SysUserDept.Type.Main)
                ).map(sysUserDept -> true));
                return Flux.fromIterable(monos).flatMap(Function.identity()).all(Boolean::booleanValue);
            }).filter(aBoolean -> aBoolean).switchIfEmpty(Mono.error(new BusinessException("保存用户失败"))),
            DB.beginTransaction());
    }


    private Mono<List<SysRole>> checkRole(List<String> roles) {
        return sysLoginRoleReactiveService.findAll(new QSysRole().id.in(roles))
            .filter(sysRoles -> sysRoles.size() == roles.size())
            .switchIfEmpty(Mono.error(new BusinessException("用户组错误")));
    }

    public Mono<Boolean> remove(String id) {
        return transactionOf(
            sysUserDeptReactiveService.deleteReactive(new QSysUserDept().userId.eq(id))
                .then(sysUserRoleService.deleteReactive(new QSysUserRole().userId.eq(id)))
                .filter(aBoolean -> true)
                .switchIfEmpty(Mono.just(true))
                .flatMap(aBoolean -> deleteByIdReactive(id)),
            DB.beginTransaction()
        ).filter(aBoolean -> aBoolean).switchIfEmpty(Mono.error(new BusinessException("删除失败")));
    }

    public Mono<Boolean> update(SysUser user, String mainDeptId, List<String> roleIds) {
        return transactionOf(
            checkRole(roleIds)
                .zipWith(this.updateReactive(user))
                .filter(tuple -> tuple.getT2())
                .switchIfEmpty(Mono.error(new BusinessException("更新用户基本信息失败")))
                .flatMap(userUpdateResultTuple -> {
                    Mono<SysUserDept> currentMainDeptMono = sysUserDeptReactiveService.findOneReactive(
                        new QSysUserDept().userId.eq(user.getId()).type.eq(SysUserDept.Type.Main)
                    );
                    return currentMainDeptMono.map(Optional::of).defaultIfEmpty(Optional.empty())
                        .flatMap(optionalCurrentMainDept -> {
                            List<Mono<Boolean>> deptOperations = new ArrayList<>();
                            if (optionalCurrentMainDept.isPresent()) {
                                SysUserDept currentMainDept = optionalCurrentMainDept.get();
                                if (!currentMainDept.getDeptId().equals(mainDeptId)) {
                                    currentMainDept.setType(SysUserDept.Type.Sub);
                                    deptOperations.add(
                                        sysUserDeptReactiveService.updateReactive(currentMainDept).map(ud -> true)
                                    );
                                    deptOperations.add(
                                        sysUserDeptReactiveService.saveReactive(
                                            new SysUserDept(new SysUserDeptId(user.getId(), mainDeptId), SysUserDept.Type.Main)
                                        ).map(ud -> true)
                                    );
                                }
                            } else {
                                deptOperations.add(
                                    sysUserDeptReactiveService.saveReactive(
                                        new SysUserDept(new SysUserDeptId(user.getId(), mainDeptId), SysUserDept.Type.Main)
                                    ).map(ud -> true)
                                );
                            }
                            if (deptOperations.isEmpty()) {
                                return Mono.just(true);
                            }
                            return Flux.concat(deptOperations).all(Boolean::booleanValue);
                        });
                })
                .zipWhen(deptUpdated ->
                    sysUserRoleService.findAllReactive(new QSysUserRole().userId.eq(user.getId()))
                        .flatMap(userRoles -> {
                            List<Mono<Boolean>> roleOperations = new ArrayList<>();
                            ListUtil.diff(roleIds, userRoles,
                                (roleIdFromParam, userRoleEntity) -> userRoleEntity.getRoleId().equals(roleIdFromParam),
                                rolesToAdd -> {
                                    for (String roleIdToAdd : rolesToAdd) {
                                        roleOperations.add(sysUserRoleService.saveReactive(new SysUserRole(user.getId(), roleIdToAdd))
                                            .map(sysUserRole -> true));
                                    }
                                },
                                null,
                                rolesToRemove -> {
                                    if (!rolesToRemove.isEmpty()) {
                                        roleOperations.add(sysUserRoleService.deleteReactive(new QSysUserRole().id.in(
                                            rolesToRemove.stream().map(SysUserRole::getId).collect(Collectors.toSet()))));
                                    }
                                });
                            if (roleOperations.isEmpty()) {
                                return Mono.just(true);
                            }
                            return Flux.fromIterable(roleOperations).flatMap(Function.identity()).all(Boolean::booleanValue);
                        })
                )
                .map(results -> results.getT1() && results.getT2())
                .filter(aBoolean -> aBoolean)
                .switchIfEmpty(Mono.error(new BusinessException("更新失败"))),
            DB.beginTransaction()
        );
    }

    public Mono<Integer> checkUserName(String username) {
        return countReactive(new QSysUser().username.eq(username)).filter(integer -> integer <= 0)
            .switchIfEmpty(Mono.error(new BusinessException("用户名已被使用")));
    }
}
