package org.start2do.controller.webflux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.IdReq;
import org.start2do.dto.MenuResp;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.RoleDtoMapper;
import org.start2do.dto.req.role.RoleAddReq;
import org.start2do.dto.req.role.RoleMenuReq;
import org.start2do.dto.req.role.RolePageReq;
import org.start2do.dto.req.role.RoleUpdateReq;
import org.start2do.dto.req.role.RoleUserAddReq;
import org.start2do.dto.resp.role.RoleDetailResp;
import org.start2do.dto.resp.role.RolePageResp;
import org.start2do.dto.resp.role.RoleUsersResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.service.webflux.SysRoleReactiveService;
import org.start2do.service.webflux.SysUserReactiveService;
import org.start2do.service.webflux.SysUserRoleReactiveService;
import org.start2do.util.BeanValidatorUtil;
import reactor.core.publisher.Mono;

/**
 * 角色管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("role")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "role", havingValue = "true")
@ConditionalOnWebApplication(type = Type.REACTIVE)

public class SysRoleController {

    private final SysRoleReactiveService sysRoleService;
    private final SysUserRoleReactiveService userRoleService;
    private final SysUserReactiveService userService;

    /**
     * 分页
     */
    @GetMapping("page")
    public Mono<R<Page<RolePageResp>>> page(Page page, RolePageReq req) {
        QSysRole qClass = new QSysRole();
        Where.ready().like(req.getRoleName(), qClass.name::like).like(req.getRoleCode(), qClass.roleCode::like);
        return sysRoleService.page(qClass, page, RoleDtoMapper.INSTANCE::toRolePageResp).map(R::ok);
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public Mono<R<Boolean>> add(@RequestBody RoleAddReq req) {
        BeanValidatorUtil.validate(req);
        return sysRoleService.save(RoleDtoMapper.INSTANCE.toEntity(req)).map(sysRole -> true).map(R::ok);
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public Mono<R<Boolean>> update(@RequestBody RoleUpdateReq req) {
        BeanValidatorUtil.validate(req);
        return sysRoleService.getById(req.getId()).flatMap(role -> {
            RoleDtoMapper.INSTANCE.update(role, req);
            return sysRoleService.update(role);
        }).map(sysRole -> true).map(R::ok);
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public Mono<R<Boolean>> delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        return sysRoleService.remove(req.getId()).map(R::ok);
    }

    /**
     * 设置权限
     */
    @PostMapping("set")
    public Mono<R<Boolean>> set(@RequestBody RoleMenuReq req) {
        BeanValidatorUtil.validate(req);
        return sysRoleService.set(req.getRoleId(), req.getMenuIds()).map(R::ok);
    }

    /**
     * 角色菜单
     */
    @GetMapping("menu/role")
    public Mono<R<List<MenuResp>>> roleMenu() {
        return sysRoleService.findAll().map(sysRoles -> {
            return sysRoles.stream().map(sysRole -> new MenuResp(sysRole.getName(), sysRole.getId())).collect(
                Collectors.toList());
        }).map(R::ok);
    }


    /**
     * 详情
     */
    @GetMapping("detail")
    public Mono<R<RoleDetailResp>> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        return sysRoleService.getById(req.getId()).map(RoleDtoMapper.INSTANCE::toRoleDetailResp).map(R::ok);
    }


    /**
     * 根据用户组获取该用户组下面的菜单
     */
    @GetMapping("users")
    public Mono<R<List<RoleUsersResp>>> users(Integer roleId) {
        return userRoleService.findAll(new QSysUserRole().roleId.eq(roleId))
            .filter(sysUserRoles -> !sysUserRoles.isEmpty())
            .switchIfEmpty(Mono.just(new ArrayList<>()))
            .zipWhen(all -> userService.findAll(
                new QSysUser().id.in(
                    new QSysUserRole().select(QSysUserRole.alias().userId).roleId.eq(roleId).query()
                ))).map(objects -> {
                List<SysUserRole> all = objects.getT1();
                List<SysUser> allUsers = objects.getT2();
                Map<Integer, SysUser> map = allUsers.stream().collect(Collectors.toMap(SysUser::getId, e -> e));
                return all.stream().map(t -> {
                    SysUser user = map.get(t.getUserId());
                    return new RoleUsersResp(t.getUserId(),
                        Optional.ofNullable(user).map(SysUser::getUsername).orElse(null),
                        Optional.ofNullable(user).map(SysUser::getRealName).orElse(null)
                    );
                }).collect(Collectors.toList());
            }).map(R::ok);

    }

    /**
     * 设置用户组
     */
    @PostMapping("set/user")
    public Mono<R<Boolean>> setUserRole(@RequestBody RoleUserAddReq req) {
        BeanValidatorUtil.validate(req);
        return userRoleService.save(req.getRoleId(), req.getUserId()).map(R::ok);
    }

}
