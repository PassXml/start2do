package org.start2do;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.start2do.config.KaptchaConfig;
import org.start2do.dto.CustomContextInfo;
import org.start2do.dto.req.login.IPasswordText;
import org.start2do.dto.req.login.JwtRequest;
import org.start2do.service.IRestPwService;
import org.start2do.util.JwtTokenUtil;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Configuration(proxyBeanMethods = false)
@ComponentScan("org.start2do")
@Import({Start2doSecurityConfig.class, KaptchaConfig.class})
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
@RequiredArgsConstructor
public class AutoScanConfig {

    @Bean
    @ConditionalOnProperty(prefix = "jwt",name = "enable-password-encoder",havingValue = "true")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final Start2doSecurityConfig start2doSecurityConfig;

    @PostConstruct
    public void init() {
        JwtTokenUtil.SECRET = start2doSecurityConfig.getSecret();
    }

    @Bean
    @ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    @ConditionalOnMissingBean(IRestPwService.class)
    @ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
    public IRestPwService iRestPwService() {
        return new IRestPwService() {
            @Override
            public void sendValidateEmailCode(String username, String email) {

            }

            @Override
            public void sendValidateSMSCode(String username, String phone) {

            }

            @Override
            public void validateCode(String username, String verificationCode) {

            }
        };
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
                return null;
            }

            @Override
            public Context injectContext(Context context, String jwtStr, Integer tenantId, Object otherInfo) {
                return null;
            }

            @Override
            public Mono<Object> injectOtherInfo(String jwtStr) {
                return null;
            }

            @Override
            public void loadReqBefore(IPasswordText req) {

            }
        };
    }


}
