package org.start2do.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.config.KaptchaConfig;
import org.start2do.dto.BusinessException;
import org.start2do.dto.R;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.req.login.JwtRequest;
import org.start2do.dto.resp.login.AuthRoleMenuResp;
import org.start2do.dto.resp.login.JwtResponse;
import org.start2do.ebean.dto.EnableType;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.service.SysLoginMenuService;
import org.start2do.service.imp.SysLoginUserServiceImpl;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;
import org.start2do.util.spring.RedisCacheUtil;
import reactor.core.publisher.Mono;

/**
 * 登录
 */
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnExpression("${jwt.enable:false}")
public class WebFluxLoginController {

    @Lazy
    @Autowired
    private ReactiveAuthenticationManager authenticationManage;

    private final SysLoginMenuService sysLoginMenuService;
    private final SysLoginUserServiceImpl userDetailsService;
    private final KaptchaConfig config;

    /**
     * 登录
     */
    @PostMapping(value = "/login")
    public Mono<R<JwtResponse>> createAuthenticationToken(@RequestBody JwtRequest req) {
        return Mono.fromCallable(() -> {
            BeanValidatorUtil.validate(req);
            if (config.getEnable() != null && config.getEnable()) {
                if (StringUtils.isEmpty(req.getKaptchaCode()) || StringUtils.isEmpty(req.getKaptchaKey())) {
                    throw new BusinessException("验证码不能为空");
                }
                String kaptcha = Optional.ofNullable(RedisCacheUtil.get(KaptchaController.KEY + req.getKaptchaKey()))
                    .map(Object::toString)
                    .orElseThrow(() -> new BusinessException("验证码已超时,请重新刷新验证码"));
                if (!req.getKaptchaCode().equals(kaptcha)) {
                    throw new BusinessException("验证码不正确");
                }
            }
            authenticate(req.getUsername(), req.getPassword());
            UserCredentials userCredentials = userDetailsService.loadUserByUsername(req.getUsername());
            JwtResponse response = new JwtResponse(userCredentials, JwtTokenUtil.generateToken(userCredentials));
            return R.ok(response);
        });
    }

    /**
     * 登出
     */
    @GetMapping("/logout")
    public Mono<R<String>> logout() {
        return Mono.just(R.ok());
    }

    /**
     * 检查token
     */
    @GetMapping("/check_token")
    public Mono<R<String>> checkToken() {
        return Mono.just(R.ok());
    }

    /**
     * 用户菜单
     */
    @GetMapping("menu")
    public Mono<R<List<AuthRoleMenuResp>>> menu() {
        return Mono.fromCallable(() -> {
            List<SysMenu> menus = sysLoginMenuService.findAll(
                new QSysMenu().status.eq(EnableType.Enable).roles.users.id.eq(JwtTokenUtil.getUserId()));
            return R.ok(menus.stream().map(AuthRoleMenuResp::new).collect(Collectors.toList()));
        });
    }


    private void authenticate(String username, String password) {
        try {
            authenticationManage.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new BusinessException("用户未启用");
        } catch (BadCredentialsException e) {
            throw new BusinessException("密码错误");
        }
    }
}
