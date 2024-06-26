package org.start2do;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.start2do.dto.req.login.JwtRequest;
import org.start2do.filter.JwtRequestWebFluxFilter.CustomContextInfo;
import org.start2do.service.imp.SysLoginUserCustomInfoEmptyReactiveService;
import org.start2do.service.reactive.ISysLoginUserCustomInfoReactiveService;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

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
        JwtTokenUtil.JWT_TOKEN_VALIDITY = start2doSecurityConfig.getJwtTokenValidity();
        JwtTokenUtil.MockUser = start2doSecurityConfig.getMockUser();
        JwtTokenUtil.MockUserId = start2doSecurityConfig.getMockUserId();
        JwtTokenUtil.MockUserName = start2doSecurityConfig.getMockUserName();
        JwtTokenUtil.IsWebFlux = WebApplicationType.REACTIVE == start2doSecurityConfig.getWebApplicationType();
    }

    @Bean
    @ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    @ConditionalOnMissingBean(ISysLoginUserCustomInfoReactiveService.class)
    public SysLoginUserCustomInfoEmptyReactiveService sysLoginUserCustomInfoEmptyReactiveService() {
        return new SysLoginUserCustomInfoEmptyReactiveService();
    }

    @Bean
    @ConditionalOnMissingBean(CustomContextInfo.class)
    @ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
    public CustomContextInfo customContextInfo() {
        return new CustomContextInfo() {

            @Override
            public void loadReqBefore(JwtRequest request) {

            }

            @Override
            public <R> Mono<R> loadUserBefore(Mono<R> mono) {
                return mono;
            }

            @Override
            public Context injectContext(Context context, String jwtStr, Integer tenantId, Object otherInfo) {
                return context;
            }

            @Override
            public Mono<Object> injectOtherInfo(String jwtStr) {
                return Mono.just("");
            }
        };
    }
}
