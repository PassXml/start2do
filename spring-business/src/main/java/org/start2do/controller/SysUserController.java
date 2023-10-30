package org.start2do.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.BusinessException;
import org.start2do.dto.IdReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.mapper.UserDtoMapper;
import org.start2do.dto.req.user.UserAddReq;
import org.start2do.dto.req.user.UserPageReq;
import org.start2do.dto.req.user.UserStatusReq;
import org.start2do.dto.req.user.UserUpdateReq;
import org.start2do.dto.resp.user.UserDetailResp;
import org.start2do.dto.resp.user.UserPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.SysRoleService;
import org.start2do.service.SysUserService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.StringUtils;

/**
 * 用户管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class SysUserController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final SysRoleService sysRoleService;


    /**
     * 分页
     */
    @GetMapping("page")
    public R<Page<UserPageResp>> page(UserPageReq req) {
        QSysUser qClass = new QSysUser();
        Where.ready().like(req.getUsername(), qClass.username::like)
            .notNull(req.getRole(), qClass.roles.id::eq);
        return R.ok(sysUserService.page(qClass, req, UserDtoMapper.INSTANCE::toUserPageResp));
    }

    /**
     * 添加
     */
    @PostMapping("add")
    public R add(@RequestBody UserAddReq req) {
        System.out.println(req.getClass());
        BeanValidatorUtil.validate(req);
        if (StringUtils.isEmpty(req.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        sysUserService.checkUserName(req.getUsername());
        sysUserService.add(UserDtoMapper.INSTANCE.toEntity(req), req.getRoles());
        return R.ok();
    }

    /**
     * 更新
     */
    @PostMapping("update")
    public R update(@RequestBody UserUpdateReq req) {
        BeanValidatorUtil.validate(req);
        SysUser user = sysUserService.getById(req.getId());
        UserDtoMapper.INSTANCE.update(user, req);
        if (StringUtils.isEmpty(req.getPassword())) {
            user.setPassword(user.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        sysUserService.update(user, req.getRoles());
        return R.ok();
    }

    /**
     * 删除
     */
    @GetMapping("delete")
    public R delete(IdReq req) {
        BeanValidatorUtil.validate(req);
        sysUserService.remove(req.getId());
        return R.ok();
    }

    /**
     * 详情
     */
    @GetMapping("detail")
    public R<UserDetailResp> detail(IdReq req) {
        BeanValidatorUtil.validate(req);
        SysUser user = sysUserService.getOne(new QSysUser().id.eq(req.getId()).roles.fetchQuery());
        UserDetailResp resp = UserDtoMapper.INSTANCE.toUserDetailResp(user);
        List<SysRole> roles = sysRoleService.findAll(new QSysRole().users.id.eq(user.getId()));
        resp.setRoles(roles.stream().map(SysRole::getId).collect(Collectors.toList()));
        List<Integer> menuIds = new ArrayList<>();
        for (SysRole role : user.getRoles()) {
            menuIds.addAll(role.getMenus().stream().map(SysMenu::getId).collect(Collectors.toList()));
        }
        resp.setMenus(menuIds);
        return R.ok(resp);
    }

    /**
     * 修改状态
     */
    @PostMapping("status")
    public R status(UserStatusReq req) {
        BeanValidatorUtil.validate(req);
        SysUser user = sysUserService.getById(req.getId());
        user.setStatus(req.getType());
        sysUserService.update(user);
        return R.ok();
    }
}
