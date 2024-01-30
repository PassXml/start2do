package org.start2do;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.start2do.config.KaptchaConfig;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;

@Slf4j
@Import({Start2doSecurityConfig.class, KaptchaConfig.class})
@AutoConfiguration
@RequiredArgsConstructor
@ComponentScans(value = {@ComponentScan(value = "org.start2do"), @ComponentScan(value = "org.start2do.*"),
    @ComponentScan(value = "org.start2do.controller"),})
public class SecurityAutoConfiguration {

    private final Start2doSecurityConfig start2doSecurityConfig;


    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnExpression("${jwt.enable:false}")
    SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(),
            new HttpSessionSecurityContextRepository());
    }


    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnExpression("${jwt.enable:false}")
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnExpression("${jwt.enable:false}")
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
        UserDetailsService userDetailService, AuthenticationProvider authenticationProvider) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class)
            .authenticationProvider(authenticationProvider);
        builder.userDetailsService(userDetailService).passwordEncoder(bCryptPasswordEncoder);
        return builder.build();
    }


    @Bean
    @ConditionalOnMissingBean(value = {PasswordEncoder.class})
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        String secret = start2doSecurityConfig.getSecret();
        if (StringUtils.isEmpty(secret)) {
            secret = org.start2do.util.JwtTokenUtil.genKey();
            start2doSecurityConfig.setSecret(secret);
            log.info("生成密钥:{}", secret);
        }
        JwtTokenUtil.SECRET = secret;
        JwtTokenUtil.MockUser = start2doSecurityConfig.getMockUser();
        JwtTokenUtil.MockUserId = start2doSecurityConfig.getMockUserId();
        JwtTokenUtil.MockUserName = start2doSecurityConfig.getMockUserName();
        JwtTokenUtil.IsWebFlux = WebApplicationType.REACTIVE == start2doSecurityConfig.getWebApplicationType();
    }
}
