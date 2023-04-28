package org.start2do.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.start2do.Start2doSecurityConfig;
import org.start2do.filter.JwtRequestFilter;
import org.start2do.util.JwtTokenUtil;


@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final Start2doSecurityConfig config;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

//    @Bean
//    @ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
//    public AuthenticationManager authenticationManagerBean(AuthenticationManagerBuilder auth,
//        UserDetailsService jwtUserDetailsService) throws Exception {
//        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
//        return auth.build();
//    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity, JwtRequestFilter jwtRequestFilter,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
        if (config.getEnable()) {
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry security = httpSecurity.csrf()
                .disable().authorizeHttpRequests().requestMatchers("/auth/login", "/auth/code").permitAll();
            if (config.getCheckExpired() != null) {
                JwtTokenUtil.CheckExpired = config.getCheckExpired();
            }
            if (config.getWhiteList() != null) {
                security.requestMatchers(config.getWhiteList().toArray(new String[]{})).permitAll();
            }
            security.anyRequest().authenticated().and().exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            return httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class).build();
        }
        return httpSecurity.build();
    }
}
