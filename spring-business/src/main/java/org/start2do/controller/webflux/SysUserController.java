package org.start2do.controller.webflux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.start2do.dto.req.user.UserAddReq;
import org.start2do.dto.req.user.UserMenuReq;
import org.start2do.dto.req.user.UserMenuResp;
import org.start2do.dto.req.user.UserPageReq;
import org.start2do.dto.req.user.UserStatusReq;
import org.start2do.dto.req.user.UserUpdateReq;
import org.start2do.dto.resp.user.UserDetailResp;
import org.start2do.dto.resp.user.UserDetailResp.Item;
import org.start2do.dto.resp.user.UserPageResp;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.SysRole;
import org.start2do.entity.security.query.QSysRole;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.webflux.SysRoleReactiveService;
import org.start2do.service.webflux.SysUserReactiveService;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.dto.req.user.UpdateCurrentUserInfoDto;
import org.start2do.dto.resp.user.CurrentUserInfoDto;
import org.start2do.entity.security.SysDept;
import org.start2do.util.JwtTokenUtil; // 注意：WebFlux 环境下需要替换为 ReactiveSecurityContextHolder 等方式获取用户信息
import org.start2do.util.StringUtils;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
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
@ConditionalOnWebApplication(type = Type.REACTIVE)
@Permission(groupName = "用户管理")
public class SysUserController {

  private final SysUserReactiveService sysUserService;
  private final PasswordEncoder passwordEncoder;
  private final SysRoleReactiveService sysRoleService;

  /** 分页 */
  @GetMapping("page")
  public Mono<R<Page<UserPageResp>>> page(UserPageReq req) {
    QSysUser qClass = new QSysUser().roles.fetch();
    Where.ready()
        .like(req.getUsername(), qClass.username::like)
        .like(req.getRealName(), qClass.realName::like)
        .notEmpty(req.getRole(), qClass.roles.id::eq); // 与 servlet 版本对齐
    return sysUserService
        .pageReactive(qClass, req, UserDtoMapper.INSTANCE::toUserPageResp)
        .map(R::ok);
  }

  /** 添加 */
  @PostMapping("add")
  @SysLogSetting("添加用户")
  public Mono<R<Void>> add(@RequestBody UserAddReq req) { // 返回类型与 servlet 版本对齐
    BeanValidatorUtil.validate(req);
    if (StringUtils.isEmpty(req.getPassword())) {
      throw new BusinessException("密码不能为空");
    }
    return sysUserService
        .checkUserName(req.getUsername())
        // 假设 UserAddReq 有 getDeptId()，并且服务层 add 方法支持 deptId
        .then(sysUserService.add(UserDtoMapper.INSTANCE.toEntity(req), req.getDeptId(), req.getRoles()))
        .thenReturn(R.ok()); // 与 servlet 版本对齐
  }

  /** 更新 */
  @PostMapping("update")
  @SysLogSetting("更新用户")
  public Mono<R<Void>> update(@RequestBody UserUpdateReq req) { // 返回类型与 servlet 版本对齐
    BeanValidatorUtil.validate(req);
    return sysUserService
        .getByIdReactive(req.getId())
        .flatMap(
            user -> {
              UserDtoMapper.INSTANCE.update(user, req);
              if (StringUtils.isEmpty(req.getPassword())) {
                user.setPassword(user.getPassword());
              } else {
                user.setPassword(passwordEncoder.encode(req.getPassword()));
              }
              // 假设 UserUpdateReq 有 getDeptId()，并且服务层 update 方法支持 deptId
              return sysUserService.update(user, req.getDeptId(), req.getRoles());
            })
        .thenReturn(R.ok()); // 与 servlet 版本对齐
  }

  /** 删除 */
  @GetMapping("delete")
  @SysLogSetting("删除")
  public Mono<R<Void>> delete(IdStrReq req) { // 返回类型与 servlet 版本对齐
    BeanValidatorUtil.validate(req);
    return sysUserService.remove(req.getId()).thenReturn(R.ok()); // 与 servlet 版本对齐
  }

  /** 详情 */
  @GetMapping("detail")
  public Mono<R<UserDetailResp>> detail(IdStrReq req) {
    BeanValidatorUtil.validate(req);
    return sysUserService
        .getOneReactive(new QSysUser().id.eq(req.getId()).roles.fetch())
        .map(
            user -> {
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
              return resp;
            })
        .map(R::ok);
  }

  /** 修改状态 */
  @SysLogSetting("修改状态")
  @PostMapping("status")
  public Mono<R<Void>> status(UserStatusReq req) { // 返回类型与 servlet 版本对齐
    BeanValidatorUtil.validate(req);
    return sysUserService
        .getByIdReactive(req.getId())
        .map(
            sysUser -> {
              sysUser.setStatus(req.getType());
              return sysUser;
            })
        .flatMap(sysUserService::updateReactive)
        .thenReturn(R.ok()); // 与 servlet 版本对齐
  }

  /** 用户菜单 */
  @GetMapping("menu")
  public Mono<R<List<UserMenuResp>>> menu(UserMenuReq req) { // 返回类型与 servlet 版本对齐
    QSysUser qClass = new QSysUser();
    Where.ready().like(req.getRealName(), qClass.realName).like(req.getUsername(), qClass.username);
    return sysUserService
        .findAllReactive(qClass)
        .map(
            sysUsers ->
                sysUsers.stream()
                    .map(t -> new UserMenuResp(t.getId(), t.getUsername(), t.getRealName()))
                    .collect(Collectors.toList())) // 与 servlet 版本对齐
        .map(R::ok);
  }
  /** 获取当前登录用户信息 */
  @GetMapping("profile")
  public Mono<R<CurrentUserInfoDto>> profile() {
    // 注意: 下面的 JwtTokenUtil.getUserId() 调用需要替换为 WebFlux 环境下获取用户ID的正确方式。
    // 例如: ReactiveSecurityContextHolder.getContext().map(ctx -> ctx.getAuthentication().getName())
    // 这里使用 Mono.defer 避免 JwtTokenUtil.getUserId() 在非请求线程中立即执行（如果它依赖 ThreadLocal）
    return Mono.defer(() -> Mono.justOrEmpty(JwtTokenUtil.getUserId()))
        .switchIfEmpty(Mono.error(new BusinessException("无法获取当前用户信息，用户未登录或会话已过期")))
        .flatMap(
            userId ->
                sysUserService
                    .getOneReactive(new QSysUser().id.eq(userId).dept.fetch())
                    .switchIfEmpty(Mono.error(new BusinessException("用户不存在或已被删除")))
                    .map(
                        user -> {
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
                          List<SysDept> depts = user.getDept();
                          if (dept != null && depts != null) {
                            dto.setDeptNames(
                                depts.stream()
                                    .filter(d -> dept.getId().equals(d.getId()))
                                    .map(SysDept::getName)
                                    .toList());
                          } else {
                            dto.setDeptNames(new ArrayList<>());
                          }
                          return dto;
                        }))
        .map(R::ok);
  }

  /** 更新当前登录用户信息 */
  @PostMapping("/profile/update")
  @SysLogSetting("更新当前用户信息")
  public Mono<R<Void>> updateProfile(@Valid @RequestBody UpdateCurrentUserInfoDto req) {
    // 注意: 下面的 JwtTokenUtil.getUserId() 调用需要替换为 WebFlux 环境下获取用户ID的正确方式。
    return Mono.defer(() -> Mono.justOrEmpty(JwtTokenUtil.getUserId()))
        .switchIfEmpty(Mono.error(new BusinessException("无法获取当前用户信息，用户未登录或会话已过期")))
        .flatMap(
            currentUserId ->
                sysUserService
                    .getByIdReactive(currentUserId)
                    .switchIfEmpty(Mono.error(new BusinessException("用户不存在或已被删除")))
                    .flatMap(
                        user -> {
                          boolean changed = false;
                          if (StringUtils.isNotEmpty(req.getRealName())
                              && !req.getRealName().equals(user.getRealName())) {
                            user.setRealName(req.getRealName());
                            changed = true;
                          }
                          if (StringUtils.isNotEmpty(req.getUserPhone())
                              && !req.getUserPhone().equals(user.getPhone())) {
                            user.setPhone(req.getUserPhone());
                            changed = true;
                          }
                          if (StringUtils.isNotEmpty(req.getUserEmail())
                              && !req.getUserEmail().equals(user.getEmail())) {
                            user.setEmail(req.getUserEmail());
                            changed = true;
                          }
                          if (StringUtils.isNotEmpty(req.getAvatar())
                              && !req.getAvatar().equals(user.getAvatar())) {
                            user.setAvatar(req.getAvatar());
                            changed = true;
                          }
                          return changed ? sysUserService.updateReactive(user).then(Mono.just(true)) : Mono.just(false);
                        }))
        .thenReturn(R.ok());
  }
}
