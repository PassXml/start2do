package org.start2do.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.dto.req.permission.PermissionRoleAddReq;
import org.start2do.dto.req.permission.PermissionRolePageReq;
import org.start2do.dto.req.permission.PermissionUserAddReq;
import org.start2do.dto.req.permission.PermissionUserPageReq;
import org.start2do.dto.resp.permission.PermissionUserPageResp;
import org.start2do.entity.security.SysPermissionUserRef;
import org.start2do.entity.security.SysPermissionUserRefId;
import org.start2do.entity.security.query.QSysPermission;
import org.start2do.service.PermissionService;
import org.start2do.util.BeanValidatorUtil;

@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("reloadAllUrls")
    public R reloadAllUrls() {
        permissionService.reloadAllUrls();
        return R.ok();
    }

    /**
     * 用户权限分页查询
     */
    @GetMapping("user/page")
    public R<Page<PermissionUserPageResp>> userPage(PermissionUserPageReq req) {
        QSysPermission qClass = new QSysPermission().or().users.id.eq(req.getUserId()).roles.users.id.eq(
            req.getUserId()).endOr();
        return R.ok(permissionService.page(qClass, req, PermissionUserPageResp::new));
    }

    /**
     * 添加用户权限
     */
    @PostMapping("user/add")
    @SysLogSetting("添加用户权限")
    public R<Void> addUserPermission(@Valid @RequestBody PermissionUserAddReq req) {
        new SysPermissionUserRef(new SysPermissionUserRefId(req.getPermissionId(), req.getUserId())).save();
        return R.ok();
    }


    /**
     * 删除用户权限
     */
    @GetMapping("user/delete")
    @SysLogSetting("删除用户权限")
    public R<Void> deleteUserPermission(PermissionUserAddReq req) {
        BeanValidatorUtil.validate(req);
        permissionService.removeUserPermission(req.getUserId(), req.getPermissionId());
        return R.ok();
    }

    /**
     * 角色权限分页查询
     */
    @GetMapping("role/page")
    public R<Page<PermissionUserPageResp>> rolePage(PermissionRolePageReq req) {
        QSysPermission qClass = new QSysPermission().or().roles.users.id.eq(req.getRoleId()).endOr();
        Page<PermissionUserPageResp> page = permissionService.page(qClass, req, PermissionUserPageResp::new);
        return R.ok(page);
    }

    /**
     * 添加角色权限
     */
    @PostMapping("role/add")
    @SysLogSetting("添加角色权限")
    public R<Void> addRolePermission(@Valid @RequestBody PermissionRoleAddReq req) {
        permissionService.addRolePermission(req.getRoleId(), req.getPermissionId());
        return R.ok();
    }

    /**
     * 删除角色权限
     */
    @GetMapping("role/delete")
    @SysLogSetting("删除角色权限")
    public R<Void> deleteRolePermission(@Valid PermissionRoleAddReq req) {
        BeanValidatorUtil.validate(req);
        permissionService.deleteRolePermission(req.getRoleId(), req.getPermissionId());
        return R.ok();
    }
}
