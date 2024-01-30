package org.start2do.service.webflux;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.service.AbsReactiveService;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysRoleMenu;
import org.start2do.entity.security.SysRoleMenuId;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysRoleMenu;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.util.ListUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({DataSourceProperties.class})

@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysRoleReactiveService extends AbsReactiveService<SysRole, Integer> {

    private final SysRoleMenuReactiveService sysRoleMenuService;
    private final SysMenuReactiveService sysMenuService;
    private final SysUserReactiveService sysUserService;

    public Mono<Boolean> remove(Integer id) {
        return sysMenuService.count(new QSysMenu().roles.id.eq(id)).filter(integer -> integer <= 0)
            .switchIfEmpty(Mono.error(new BusinessException("请先取消权限")))
            .then(sysUserService.count(new QSysUser().roles.id.eq(id))).filter(integer -> integer <= 0)
            .switchIfEmpty(Mono.error(new BusinessException("用户组用户不为空"))).then(deleteById(id));
    }

    public Mono<Boolean> set(Integer roleId, List<Integer> menuIds) {
        return Mono.just(menuIds != null && !menuIds.isEmpty()).flatMap(aBoolean -> {
                if (Boolean.TRUE.equals(aBoolean)) {
                    return sysMenuService.count(new QSysMenu().id.in(menuIds)).filter(integer -> integer == menuIds.size())
                        .switchIfEmpty(Mono.error(new BusinessException("数据有误,请刷新页面")));
                }
                return Mono.just(true);
            }).then(count(new QSysRole().id.eq(roleId)).filter(integer -> integer > 0)
                .switchIfEmpty(Mono.error(new BusinessException("数据有误,请刷新页面"))))
            .then(sysRoleMenuService.findAll(new QSysRoleMenu().id.roleId.eq(roleId))).flatMap(menus -> {
                List<Mono<Boolean>> result = new ArrayList<>();
                ListUtil.diff(menuIds, menus, (integer, sysRoleMenu) -> integer.equals(sysRoleMenu.getId().getMenuId()),
                    integers -> {
                        for (Integer integer : integers) {
                            result.add(sysRoleMenuService.save(new SysRoleMenu(new SysRoleMenuId(roleId, integer)))
                                .map(sysRoleMenu -> true));
                        }
                    }, null, integers -> {
                        Set<Integer> collect = integers.stream().map(SysRoleMenu::getId).map(SysRoleMenuId::getMenuId)
                            .collect(Collectors.toSet());
                        result.add(
                            sysRoleMenuService.delete(new QSysRoleMenu().id.roleId.eq(roleId).id.menuId.in(collect)));
                    });
                return Flux.fromIterable(result).flatMap(Function.identity()).all(Boolean::booleanValue);
            }).filter(aBoolean -> aBoolean).switchIfEmpty(Mono.error(new BusinessException("保存失败")));
    }
}
