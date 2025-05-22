package org.start2do.controller.servlet;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.BusinessException;
import org.start2do.dto.IdStrReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.dto.mapper.UserDtoMapper;
import org.start2do.dto.req.user.UpdateCurrentUserInfoDto;
import org.start2do.dto.req.user.UserAddReq;
import org.start2do.dto.req.user.UserMenuReq;
import org.start2do.dto.req.user.UserMenuResp;
import org.start2do.dto.req.user.UserPageReq;
import org.start2do.dto.req.user.UserStatusReq;
import org.start2do.dto.req.user.UserUpdateReq;
import org.start2do.dto.resp.user.CurrentUserInfoDto;
import org.start2do.dto.resp.user.UserDetailResp;
import org.start2do.dto.resp.user.UserDetailResp.Item;
import org.start2do.dto.resp.user.UserPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysDept;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.servlet.SysRoleService;
import org.start2do.service.servlet.SysUserService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;
import org.start2do.dto.Permission;

/** 用户管理 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@ConditionalOnProperty(
    prefix = "start2do.business.controller",
    name = "user",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.SERVLET)
@Permission(groupName = "用户管理")
public class SysUserController {

  private final SysUserService sysUserService;
  private final PasswordEncoder passwordEncoder;
  private final SysRoleService sysRoleService;

  /** 分页 */
  @GetMapping("page")
  public R<Page<UserPageResp>> page(UserPageReq req) {
    QSysUser qClass = new QSysUser().roles.fetch();
    Where.ready()
        .like(req.getRealName(), qClass.realName::like)
        .like(req.getUsername(), qClass.username::like)
        .notEmpty(req.getRole(), qClass.roles.id::eq);
    return R.ok(sysUserService.page(qClass, req, UserDtoMapper.INSTANCE::toUserPageResp));
  }

  /** 添加 */
  @PostMapping("add")
  @SysLogSetting("添加用户")
  public R<Void> add(@RequestBody UserAddReq req) {
    BeanValidatorUtil.validate(req);
    if (StringUtils.isEmpty(req.getPassword())) {
      throw new BusinessException("密码不能为空");
    }
    sysUserService.checkUserName(req.getUsername());
    sysUserService.add(UserDtoMapper.INSTANCE.toEntity(req), req.getDeptId(), req.getRoles());
    return R.ok();
  }

  /** 更新 */
  @SysLogSetting("更新用户")
  @PostMapping("update")
  public R<Void> update(@RequestBody UserUpdateReq req) {
    BeanValidatorUtil.validate(req);
    SysUser user = sysUserService.getById(req.getId());
    UserDtoMapper.INSTANCE.update(user, req);
    if (StringUtils.isEmpty(req.getPassword())) {
      user.setPassword(user.getPassword());
    } else {
      user.setPassword(passwordEncoder.encode(req.getPassword()));
    }
    sysUserService.update(user, req.getDeptId(), req.getRoles());
    return R.ok();
  }

  /** 删除 */
  @SysLogSetting("删除用户")
  @GetMapping("delete")
  public R<Void> delete(IdStrReq req) {
    BeanValidatorUtil.validate(req);
    sysUserService.remove(req.getId());
    return R.ok();
  }

  /** 详情 */
  @GetMapping("detail")
  public R<UserDetailResp> detail(IdStrReq req) {
    BeanValidatorUtil.validate(req);
    SysUser user = sysUserService.getOne(new QSysUser().id.eq(req.getId()).roles.fetch());
    UserDetailResp resp = UserDtoMapper.INSTANCE.toUserDetailResp(user);
    List<SysRole> roles =
        sysRoleService.findAll(new QSysRole().menus.fetch().users.id.eq(user.getId()));
    resp.setRoles(roles.stream().map(SysRole::getId).toList());
    resp.setRolesInfo(roles.stream().map(t -> new Item(t.getId(), t.getName())).toList());
    List<String> menuIds = new ArrayList<>();
    for (SysRole role : user.getRoles()) {
      menuIds.addAll(role.getMenus().stream().map(SysMenu::getId).toList());
    }
    resp.setMenus(menuIds);
    return R.ok(resp);
  }

  /** 修改状态 */
  @PostMapping("status")
  @SysLogSetting("修改用户状态")
  public R<Void> status(UserStatusReq req) {
    BeanValidatorUtil.validate(req);
    SysUser user = sysUserService.getById(req.getId());
    user.setStatus(req.getType());
    sysUserService.update(user);
    return R.ok();
  }

  /** 用户菜单 */
  @GetMapping("menu")
  public R<List<UserMenuResp>> menu(UserMenuReq req) {
    QSysUser qClass = new QSysUser();
    Where.ready().like(req.getRealName(), qClass.realName).like(req.getUsername(), qClass.username);
    return R.ok(
        sysUserService.findAll(qClass).stream()
            .map(t -> new UserMenuResp(t.getId(), t.getUsername(), t.getRealName()))
            .toList());
  }

  /** 获取当前登录用户信息 */
  @GetMapping("profile")
  public R<CurrentUserInfoDto> profile() {
    String userId = JwtTokenUtil.getUserId();
    if (StringUtils.isEmpty(userId)) {
      throw new BusinessException("无法获取当前用户信息，用户未登录或会话已过期");
    }
    SysUser user = sysUserService.getOne(new QSysUser().id.eq(userId).dept.fetch());
    if (user == null) {
      throw new BusinessException("用户不存在或已被删除");
    }

    CurrentUserInfoDto dto = new CurrentUserInfoDto();
    dto.setId(user.getId());
    dto.setName(user.getUsername());
    dto.setRealName(user.getRealName());
    dto.setUserPhone(user.getPhone());
    dto.setUserEmail(user.getEmail());
    dto.setAvatar(user.getAvatar());

    SysDept dept = user.getMainDept();
    if (dept != null) {
      dto.setDeptId(dept.getId());
      dto.setDeptName(dept.getName());
    }

    return R.ok(dto);
  }

  /** 更新当前登录用户信息 */
  @PostMapping("/profile/update")
  @SysLogSetting("更新当前用户信息")
  public R<Void> updateProfile(@RequestBody UpdateCurrentUserInfoDto req) {
    BeanValidatorUtil.validate(req);

    String currentUserId = JwtTokenUtil.getUserId();
    if (StringUtils.isEmpty(currentUserId)) {
      throw new BusinessException("无法获取当前用户信息，用户未登录或会话已过期");
    }

    if (!req.getId().equals(currentUserId)) {
      throw new BusinessException("无权修改他人信息");
    }

    SysUser user = sysUserService.getById(currentUserId);
    if (user == null) {
      throw new BusinessException("用户不存在或已被删除");
    }

    boolean changed = false;
    if (StringUtils.isNotEmpty(req.getRealName()) && !req.getRealName().equals(user.getRealName())) {
      user.setRealName(req.getRealName());
      changed = true;
    }
    if (StringUtils.isNotEmpty(req.getUserPhone()) && !req.getUserPhone().equals(user.getPhone())) {
      user.setPhone(req.getUserPhone());
      changed = true;
    }
    if (StringUtils.isNotEmpty(req.getUserEmail()) && !req.getUserEmail().equals(user.getEmail())) {
      user.setEmail(req.getUserEmail());
      changed = true;
    }
    if (StringUtils.isNotEmpty(req.getAvatar()) && !req.getAvatar().equals(user.getAvatar())) {
      user.setAvatar(req.getAvatar());
      changed = true;
    }
//    if (req.getEnterpriseWechat() != null && !req.getEnterpriseWechat().equals(user.getEnterpriseWechat())) {
//      user.setEnterpriseWechat(req.getEnterpriseWechat());
//      changed = true;
//    }
    
    if (changed) {
      sysUserService.update(user);
    }
    return R.ok();
  }
}
