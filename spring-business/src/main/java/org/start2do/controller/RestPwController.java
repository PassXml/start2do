package org.start2do.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.BusinessException;
import org.start2do.dto.R;
import org.start2do.dto.req.restpw.RestPwChangeReq;
import org.start2do.dto.req.restpw.RestPwReq;
import org.start2do.dto.req.restpw.RestPwReq.Type;
import org.start2do.entity.security.SysUser;
import org.start2do.entity.security.query.QSysUser;
import org.start2do.service.IRestPwService;

/**
 * 重置密码
 */
@Slf4j
@RestController
@RequestMapping("/rest/pw")
@RequiredArgsConstructor
public class RestPwController {

    private final PasswordEncoder passwordEncoder;
    private final IRestPwService iRestPwService;

    /**
     * 重置密码
     */
    @GetMapping("submit")
    public R submit(@Valid @RequestBody RestPwChangeReq req) {
        iRestPwService.validateCode(req.getUsername(), req.getVerificationCode());
        SysUser sysUser = new QSysUser().username.eq(req.getUsername()).findOneOrEmpty()
            .orElseThrow(() -> new BusinessException("用户名不存在"));
        sysUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        sysUser.update();
        return R.ok();
    }

    /**
     * 发送验证码
     */
    @PostMapping("sendValidateCode")
    public R sendValidateCode(@RequestBody @Valid RestPwReq req) {
        QSysUser qClass = new QSysUser().username.eq(req.getUsername());
        if (req.getType() == Type.Email) {
            qClass.email.eq(req.getEmail());
        } else if (req.getType() == Type.SMS) {
            qClass.phone.eq(req.getPhone());
        }
        SysUser user = qClass.findOne();
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        if (req.getType() == Type.Email) {
            iRestPwService.sendValidateEmailCode(req.getUsername(), req.getEmail());
        } else if (req.getType() == Type.SMS) {
            iRestPwService.sendValidateSMSCode(req.getUsername(), req.getPhone());
        }
        return R.ok();
    }
}
