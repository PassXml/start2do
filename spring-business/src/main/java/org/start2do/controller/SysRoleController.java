package org.start2do.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.SysUserRole;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.entity.security.query.QSysUserRole;
import org.start2do.service.SysRoleService;
import org.start2do.service.SysUserRoleService;
import org.start2do.service.SysUserService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.ListUtil;

/**
 * 角色管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("role")
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysUserRoleService userRoleService;
    private final SysUserService userService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<RolePageResp>> page(Page page, RolePageReq req) {
        QSysRole qClass = new QSysRole();
        Where.ready().like(req.getRoleName(), qClass.name::like).like(req.getRoleCode(), qClass.roleCode::like);
        return R.ok(sysRoleService.page(qClass, page, RoleDtoMapper.INSTANCE::toRolePageResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody RoleAddReq req) {
        BeanValidatorUtil.validate(req);
        sysRoleService.save(RoleDtoMapper.INSTANCE.toEntity(req));
        return R.ok();
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody RoleUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysRole role = sysRoleService.getById(req.getId());
        RoleDtoMapper.INSTANCE.update(role, req);
        sysRoleService.update(role);
        return R.ok();
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        sysRoleService.remove(req.getId());
        return R.ok();
    }

    /**
     * 设置权限
     */
    @PostMapping("set")
    public R set(@RequestBody RoleMenuReq req) {
        BeanValidatorUtil.validate(req);
        sysRoleService.set(req.getRoleId(), req.getMenuIds());
        return R.ok();
    }

    /**
     * 角色菜单
     */
    @GetMapping("menu/role")
    public R<List<MenuResp>> roleMenu() {
        return R.ok(
            sysRoleService.findAll().stream().map(sysRole -> new MenuResp(sysRole.getName(), sysRole.getId())).collect(
                Collectors.toList())
        );
    }


    /**
     * 详情
     */
    @GetMapping("detail")
    public R<RoleDetailResp> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        SysRole role = sysRoleService.getById(req.getId());
        return R.ok(RoleDtoMapper.INSTANCE.toRoleDetailResp(role));
    }


    /**
      *  根据用户组获取该用户组下面的菜单
     */
    @GetMapping("users")
    public R<List<RoleUsersResp>> users(Integer roleId) {
        List<SysUserRole> all = userRoleService.findAll(new QSysUserRole().roleId.eq(roleId));
        if (all.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        List<SysUser> allUsers = new ArrayList<>();
        ListUtil.splitAfterRun(999, all, spList -> {
            List<SysUser> users = userService.findAll(
                new QSysUser().id.in(spList.stream().map(SysUserRole::getUserId).collect(Collectors.toSet())));
            allUsers.addAll(users);
        });
        Map<Integer, SysUser> map = allUsers.stream().collect(Collectors.toMap(SysUser::getId, e -> e));
        return R.ok(all.stream().map(t -> {
            SysUser user = map.get(t.getUserId());
            return new RoleUsersResp(t.getUserId(),
                Optional.ofNullable(user).map(SysUser::getUsername).orElse(null),
                Optional.ofNullable(user).map(SysUser::getRealName).orElse(null)
            );
        }).collect(Collectors.toList()));
    }

    /**
     * 设置用户组
     */
    @PostMapping("set/user")
    public R setUserRole(@RequestBody RoleUserAddReq req) {
        BeanValidatorUtil.validate(req);
        userRoleService.save(req.getRoleId(), req.getUserId());
        return R.ok();
    }

}
