package org.start2do.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
import org.start2do.config.KaptchaConfig;
import org.start2do.dto.BusinessException;
import org.start2do.dto.R;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.req.login.JwtRequest;
import org.start2do.dto.resp.login.AuthRoleMenuResp;
import org.start2do.dto.resp.login.CodeResp;
import org.start2do.dto.resp.login.JwtResponse;
import org.start2do.entity.security.SysMenu;
import org.start2do.entity.security.query.QSysMenu;
import org.start2do.service.SysLoginMenuService;
import org.start2do.service.imp.SysLoginUserServiceImpl;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;

/**
 * ??????
 */
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final DefaultKaptcha kaptchaProducer;

    private final SysLoginMenuService sysLoginMenuService;
    private final SysLoginUserServiceImpl userDetailsService;
    @Resource
    @Lazy
    private RedisTemplate redisTemplate;
    private ValueOperations ops = null;
    private static final String Key = "Cache:Redis:Code:";
    private final KaptchaConfig config;

    @PostConstruct
    public void init() {
        if (config.getEnable()) {
            ops = redisTemplate.opsForValue();
        }
    }

    /**
     * ??????
     */
    @PostMapping(value = "/login")
    public R<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest req) {
        BeanValidatorUtil.validate(req);
        if (config.getEnable()) {
            if (StringUtils.isEmpty(req.getKaptchaCode()) || StringUtils.isEmpty(req.getKaptchaKey())) {
                throw new BusinessException("?????????????????????");
            }
            String kaptcha = Optional.ofNullable(ops.get(Key + req.getKaptchaKey())).map(Object::toString)
                .orElseThrow(() -> new BusinessException("?????????????????????"));
            if (!req.getKaptchaCode().equals(kaptcha)) {
                throw new BusinessException("??????????????????");
            }
        }
        authenticate(req.getUsername(), req.getPassword());
        UserCredentials userCredentials = userDetailsService.loadUserByUsername(req.getUsername());
        JwtResponse response = new JwtResponse(userCredentials, JwtTokenUtil.generateToken(userCredentials));
        return R.ok(response);
    }

    /**
     * ??????
     */
    @GetMapping("/logout")
    public R<String> logout() {
        return R.ok();
    }

    /**
     * ??????token
     */
    @GetMapping("/check_token")
    public R<String> checkToken() {
        return R.ok();
    }

    /**
     * ????????????
     */
    @GetMapping("menu")
    public R<List<AuthRoleMenuResp>> menu() {
        List<SysMenu> menus = sysLoginMenuService.findAll(
            new QSysMenu().roles.users.id.eq(JwtTokenUtil.getUserId()));
        return R.ok(menus.stream().map(AuthRoleMenuResp::new).collect(Collectors.toList()));
    }

    /**
     * ?????????
     */
    @SneakyThrows
    @GetMapping("/code")
    public R<CodeResp> code() {
        if (!config.getEnable()) {
            throw new BusinessException("??????????????????");
        }
        String text = kaptchaProducer.createText();
        BufferedImage bi = kaptchaProducer.createImage(text);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", outputStream);
        String key = StringUtils.randomString(12);
        String redisKey = Key + key;
        ops.set(redisKey, text, 1, TimeUnit.MINUTES);
        return R.ok(new CodeResp(key, Base64.getEncoder().encodeToString(outputStream.toByteArray())));
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new BusinessException("???????????????");
        } catch (BadCredentialsException e) {
            throw new BusinessException("????????????");
        }
    }
}
