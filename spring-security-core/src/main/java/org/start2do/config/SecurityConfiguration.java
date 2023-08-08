package org.start2do.config;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.start2do.Start2doSecurityConfig;
import org.start2do.filter.JwtRequestFilter;
import org.start2do.util.JwtTokenUtil;


@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
@Configuration
@RequiredArgsConstructor
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
            http.authorizeHttpRequests()
                //表示 /doLogin 这个地址可以不用登录直接访问
                .requestMatchers(config.getWhiteList().toArray(new String[]{})).permitAll()
                .anyRequest().authenticated().and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf().disable();
            return http.build();
            //            return http.formLogin().disable().csrf().disable().authorizeHttpRequests()
//                .requestMatchers(config.getWhiteList().toArray(new String[]{})).permitAll().anyRequest().authenticated()
//                .and().exceptionHandling().and().sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
//                .logout(t -> SecurityContextHolder.clearContext()).build();
        }
        return http.build();
    }

}
