package org.start2do.controller.servlet;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.BusinessException;
import org.start2do.dto.IdStrReq;
import org.start2do.dto.Page;
import org.start2do.dto.R;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.dto.mapper.UserAuthDtoMapper;
import org.start2do.dto.req.userauth.UserAuthPageReq;
import org.start2do.dto.resp.userauth.UserAuthDetailResp;
import org.start2do.dto.resp.userauth.UserAuthPageResp;
import org.start2do.ebean.dto.EnableType;
import org.start2do.ebean.util.Where;
import org.start2do.entity.security.SysUserAuth;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.entity.security.query.QSysUserAuth;
import org.start2do.service.servlet.SysUserAuthService;
import org.start2do.util.BeanValidatorUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("sys/user-auth")
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "user-auth", havingValue = "true", matchIfMissing = true)
public class SysUserAuthController {

    private final SysUserAuthService sysUserAuthService;

    /**
     * 分页查询第三方认证信息
     */
    @GetMapping("page")
    public R<Page<UserAuthPageResp>> page(UserAuthPageReq req) {
    QSysUserAuth q = new QSysUserAuth().user.fetch(QSysUser.alias().username);
    Where.ready()
        .like(req.getUsername(), q.user.username::like)
        .like(req.getRealName(), q.user.realName::like)
        .notEmpty(req.getUserId(), q.userId::eq)
        .like(req.getAuthType(), q.authType::like)
        .like(req.getAuthUsername(), q.authUsername::like)
        .like(req.getAuthUid(), q.authUid::like)
        .notNull(req.getStatus(), q.status::eq);

    return R.ok(sysUserAuthService.page(q, req, UserAuthDtoMapper.INSTANCE::toUserAuthPageResp));
    }

    /**
     * 查看第三方认证详情
     */
    @GetMapping("detail")
    public R<UserAuthDetailResp> detail(IdStrReq req) {
        BeanValidatorUtil.validate(req);
        SysUserAuth userAuth = sysUserAuthService.getById(req.getId());
        if (userAuth == null) {
      throw new BusinessException("第三方认证信息不存在");
        }
    return R.ok(UserAuthDtoMapper.INSTANCE.toUserAuthDetailResp(userAuth));
    }

    /**
     * 删除第三方认证信息
     */
    @SysLogSetting("删除第三方认证信息")
    @GetMapping("delete")
    public R<?> delete(IdStrReq req) {
        BeanValidatorUtil.validate(req);

        SysUserAuth userAuth = sysUserAuthService.getById(req.getId());
        if (userAuth == null) {
      throw new BusinessException("第三方认证信息不存在，无法删除");
        }
        sysUserAuthService.deleteById(req.getId());
        return R.ok();
    }

  @GetMapping("enable")
  public R<?> enable(@Valid IdStrReq req) {
    return R.ok(
        new QSysUserAuth()
            .asUpdate()
            .set(QSysUserAuth.alias().status, EnableType.Enable)
            .where()
            .idEq(req.getId())
            .update());
  }

  @GetMapping("disable")
  public R<?> disable(@Valid IdStrReq req) {
    return R.ok(
        new QSysUserAuth()
            .asUpdate()
            .set(QSysUserAuth.alias().status, EnableType.DisEnable)
            .where()
            .idEq(req.getId())
            .update());
  }
}
