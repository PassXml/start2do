package org.start2do;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.start2do.config.KaptchaConfig;
import org.start2do.util.JwtTokenUtil;

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
        JwtTokenUtil.SECRET = start2doSecurityConfig.getSecret();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
