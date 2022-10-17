package org.start2do.config;

import java.util.List;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.start2do.filter.JwtRequestFilter;
import org.start2do.util.JwtTokenUtil;


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConfigurationProperties(prefix = "jwt")
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Setter
    @Getter
    private Boolean enable = true;
    @Setter
    @Getter
    private List<String> whiteList;

    @Setter
    @Getter
    private Boolean checkExpired;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Resource
    @Lazy

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Resource
    @Lazy

    private UserDetailsService jwtUserDetailsService;

    @Resource
    @Lazy
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        if (enable) {
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry security = httpSecurity.csrf()
                .disable().authorizeRequests().antMatchers("/auth/login", "/auth/code").permitAll();
            if (this.checkExpired != null) {
                JwtTokenUtil.CheckExpired = this.checkExpired;
            }
            if (whiteList != null) {
                security.antMatchers(
                    whiteList.toArray(new String[]{})
                ).permitAll();
            }
            security.anyRequest().authenticated().and().exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        }
    }
}
