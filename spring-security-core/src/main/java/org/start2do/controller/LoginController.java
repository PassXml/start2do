package org.start2do.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.Start2doSecurityConfig;
import org.start2do.config.KaptchaConfig;
import org.start2do.dto.BusinessException;
import org.start2do.dto.R;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.req.login.JwtRequest;
import org.start2do.dto.resp.login.AuthRoleMenuResp;
import org.start2do.dto.resp.login.JwtResponse;
import org.start2do.ebean.dto.EnableType;
import org.start2do.entity.security.SysLoginLog;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.service.SysLoginMenuService;
import org.start2do.service.imp.SysLoginUserServiceImpl;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.HttpHeaderUtil;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;
import org.start2do.util.spring.RedisCacheUtil;

/**
 * 登录
 */
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnExpression("${jwt.enable:false}")
public class LoginController {

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    private final SysLoginMenuService sysLoginMenuService;
    private final SysLoginUserServiceImpl userDetailsService;
    private final KaptchaConfig config;
    private final Start2doSecurityConfig securityConfig;

    /**
     * 登录
     */
    @PostMapping(value = "/login")
    public R<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest req, HttpServletRequest request) {
        BeanValidatorUtil.validate(req);
        String username = req.getUsername();
        Integer integer = RedisCacheUtil.get(SysLoginLog.getRedisLockKey(username), () -> 0);
        if (integer > 3) {
            throw new BusinessException("短时间内登录失败次数过多,请稍后再试");
        }
        if (config.getEnable()) {
            if (StringUtils.isEmpty(req.getKaptchaCode()) || StringUtils.isEmpty(req.getKaptchaKey())) {
                throw new BusinessException("验证码不能为空");
            }
            String kaptcha = Optional.ofNullable(RedisCacheUtil.get(KaptchaController.KEY + req.getKaptchaKey()))
                .map(Object::toString).orElseThrow(() -> new BusinessException("验证码已超时,请重新刷新验证码"));
            if (!req.getKaptchaCode().equals(kaptcha)) {
                throw new BusinessException("验证码不正确");
            }
        }
        authenticate(username, req.getPassword(), request);
        UserCredentials userCredentials = userDetailsService.loadUserByUsername(username);
        JwtResponse response = new JwtResponse(userCredentials, JwtTokenUtil.generateToken(userCredentials));
        return R.ok(response);
    }

    /**
     * 登出
     */
    @GetMapping("/logout")
    public R<String> logout() {
        return R.ok();
    }

    /**
     * 检查token
     */
    @GetMapping("/check_token")
    public R<String> checkToken() {
        return R.ok();
    }

    /**
     * 用户菜单
     */
    @GetMapping("menu")
    public R<List<AuthRoleMenuResp>> menu() {
        List<SysMenu> menus = sysLoginMenuService.findAll(
            new QSysMenu().status.eq(EnableType.Enable).roles.users.id.eq(JwtTokenUtil.getUserId()));
        return R.ok(menus.stream().map(AuthRoleMenuResp::new).collect(Collectors.toList()));
    }

    private void authenticate(String username, String password, HttpServletRequest request) {
        boolean isAuthSuccess = false;
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            isAuthSuccess = true;
        } catch (DisabledException e) {
            throw new BusinessException("用户未启用");
        } catch (BadCredentialsException e) {
            throw new BusinessException("密码错误");
        } finally {
            if (!isAuthSuccess) {
                if (Boolean.TRUE.equals(securityConfig.getRecordLoginLog())) {
                    String requestIp = HttpHeaderUtil.getRealRequestIp(request);
                    String userAgent = HttpHeaderUtil.getUserAgent(request);
                    log.info("登录失败, 用户名:{}, IP:{}, User-Agent:{}", username, requestIp, userAgent);
                    RedisCacheUtil.increment(SysLoginLog.getRedisLockKey(username), 1, 1, 5,
                        TimeUnit.MINUTES);
                    new SysLoginLog(username, requestIp, userAgent).save();
                }
            }
        }
    }
}
