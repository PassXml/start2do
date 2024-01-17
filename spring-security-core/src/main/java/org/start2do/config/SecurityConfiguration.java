package org.start2do.config;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.start2do.Start2doSecurityConfig;
import org.start2do.filter.JwtRequestFilter;
import org.start2do.util.JwtTokenUtil;


@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnExpression("${jwt.enable:false}")
public class SecurityConfiguration {

    private final Start2doSecurityConfig config;
    private final JwtRequestFilter jwtRequestFilter;
    @Value("${spring.websecurity.debug:false}")
    boolean webSecurityDebug;


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (config.getEnable()) {
            if (config.getCheckExpired() != null) {
                JwtTokenUtil.CheckExpired = config.getCheckExpired();
            }
            if (config.getWhiteList() == null) {
                config.setWhiteList(new ArrayList<>());
            }
            config.getWhiteList().add("/auth/login");
            config.getWhiteList().add("/auth/code");
            http.authorizeHttpRequests(ctx -> {
                    ctx.requestMatchers(config.getWhiteList().toArray(new String[]{})).permitAll().anyRequest()
                        .authenticated();
                }).addFilterAfter(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                //表示 /doLogin 这个地址可以不用登录直接访问
                .csrf(AbstractHttpConfigurer::disable).logout(ctx -> SecurityContextHolder.clearContext());

            return http.build();
        }
        return http.build();
    }

}
