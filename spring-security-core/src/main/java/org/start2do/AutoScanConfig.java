package org.start2do;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.start2do.config.KaptchaConfig;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ComponentScan("org.start2do")
@Import({Start2doSecurityConfig.class, KaptchaConfig.class})
@RequiredArgsConstructor
public class AutoScanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final Start2doSecurityConfig start2doSecurityConfig;

    @PostConstruct
    public void init() {
        String secret = start2doSecurityConfig.getSecret();
        if (StringUtils.isEmpty(secret)) {
            secret = org.start2do.util.JwtTokenUtil.genKey();
            start2doSecurityConfig.setSecret(secret);
            log.info("生成密钥:{}", secret);
        }
        JwtTokenUtil.SECRET = secret;
    }
}
