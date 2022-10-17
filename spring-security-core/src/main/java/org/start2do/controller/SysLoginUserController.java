package org.start2do.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.dto.UserCredentials;
import org.start2do.dto.resp.login.JwtResponse;
import org.start2do.service.imp.SysLoginUserServiceImpl;
import org.start2do.util.JwtTokenUtil;
/**
  *  登录
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("user")
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
public class SysLoginUserController {

    private final SysLoginUserServiceImpl sysUserService;
    /**
      *  用户信息
     */
    @GetMapping("info")
    public R<JwtResponse> userInfo() {
        UserCredentials userCredentials = sysUserService.loadUserByUsername(JwtTokenUtil.getUserName());
        return R.ok(new JwtResponse(userCredentials, null));
    }


}
