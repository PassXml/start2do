package org.start2do.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnExpression("!${cas.is-http-security:false} && ${cas.enable:false}")
@ConditionalOnWebApplication(type = Type.SERVLET)
public class CasServletWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SingleSignOutFilter singleSignOutFilter;
    private final LogoutFilter logoutFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CasAuthenticationFilter casAuthenticationFilter;
    private final CasConfig config;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        List<String> whilteList = new ArrayList<>(
            List.of(config.getLoginUri(), config.getLogoutUri(),
                config.getCasLogoutUri(), config.getCasLoginUri())
        );
        if (config.getWhileList() != null) {
            whilteList.addAll(Arrays.asList(config.getWhileList()));
        }
        http.authorizeRequests().antMatchers(whilteList.toArray(new String[0])).permitAll().and()
            .authorizeRequests().anyRequest()
            .authenticated().and().csrf().disable().cors()
            .disable().httpBasic().authenticationEntryPoint(authenticationEntryPoint).and().logout()
            .logoutUrl(config.getLogoutUri()).logoutSuccessUrl(config.getSuccessUrl()).and()
            .addFilter(casAuthenticationFilter)
            .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class)
            .addFilterBefore(logoutFilter, LogoutFilter.class);
    }

}
