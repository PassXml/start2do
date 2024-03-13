package org.start2do.config;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.start2do.Start2doSecurityConfig;
import org.start2do.filter.JwtRequestWebFluxFilter;
import org.start2do.handle.AccessDeniedHandler;
import org.start2do.handle.AuthManagerHandler;
import org.start2do.service.reactive.SysPermissionReactiveService;
import org.start2do.util.JwtTokenUtil;


@EnableWebFluxSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnExpression("${jwt.enable:false}")
public class WebFluxSecurityConfiguration {

    private final Start2doSecurityConfig config;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public AuthManagerHandler authManagerHandler(SysPermissionReactiveService permissionReactiveService) {
        return new AuthManagerHandler(permissionReactiveService);
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
            userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**");
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
        ReactiveAuthenticationManager authenticationManager,
        JwtRequestWebFluxFilter jwtRequestWebFluxFilter) throws Exception {
        if (config.getEnable() != null && config.getEnable()) {
            if (config.getCheckExpired() != null) {
                JwtTokenUtil.CheckExpired = config.getCheckExpired();
            }
            if (config.getWhiteList() == null) {
                config.setWhiteList(new ArrayList<>());
            }
            config.getWhiteList().add("/auth/login");
            config.getWhiteList().add("/auth/code");
            http
                .formLogin(FormLoginSpec::disable)
                .httpBasic(HttpBasicSpec::disable)
                .exceptionHandling(ctx -> {
                    ctx.authenticationEntryPoint(
                            new HttpStatusServerEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(accessDeniedHandler);
                })
                .authorizeExchange(ctx -> {
                    ctx.pathMatchers(config.getWhiteList().toArray(new String[]{})).permitAll().anyExchange()
                        .authenticated();
                }).authenticationManager(authenticationManager)
                .addFilterAt(jwtRequestWebFluxFilter, SecurityWebFiltersOrder.AUTHENTICATION).csrf(CsrfSpec::disable);
            return http.build();
        }
        return http.build();
    }

}
