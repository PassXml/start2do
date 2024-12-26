package org.start2do.config;


import static org.start2do.config.CustomAuthenticationEntryPoint.REDIRECT_URL;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * CAS单点登录配置类，当配置文件中cas.enable属性为true时生效。
 */
@Import(CasConfig.class)
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(value = "cas.enable", havingValue = "true")
public class CasStater {

    /**
     * CAS配置信息
     */
    private final CasConfig config;

    /**
     * 配置认证入口点，用于未认证用户重定向到CAS登录页面。
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ServiceProperties serviceProperties) {
        CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
        //CAS登录成功后,302跳转会本服务的CAS地址
        entryPoint.setLoginUrl(config.getCasUrl() + config.getCasLoginUri());
        entryPoint.setServiceProperties(serviceProperties);
        return entryPoint;
    }


    /**
     * 配置认证管理器，使用CasAuthenticationProvider作为认证提供者。
     */
    @Bean
    protected AuthenticationManager authenticationManager(CasAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    /**
     * 配置CAS认证过滤器，用于处理CAS认证流程。
     */
    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(AuthenticationManager authenticationManager,
        AuthenticationSuccessHandler authenticationSuccessHandler, ServiceProperties serviceProperties) {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        filter.setAuthenticationManager(authenticationManager);
        filter.setServiceProperties(serviceProperties);
        //默认/login/cas
        //        filter.setFilterProcessesUrl(config.getLoginUri());
        filter.setContinueChainBeforeSuccessfulAuthentication(false);
        return filter;
    }

    /**
     * 配置服务属性，用于CAS认证过程中的服务标识。
     */
    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(config.getBaseUrl() + config.getLoginUri());
        //默认为false,但如果您的应用程序特别敏感，则应设置为 true。该参数的作用是告诉 CAS 登录服务，单点登录是不可接受的。用户需要重新输入用户名和密码才能访问服务。
        serviceProperties.setSendRenew(false);
        //验证所有的请求,而不是只认证/login/cas的请求
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    /**
     * 配置认证成功处理器，用于处理认证成功后的重定向逻辑。
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                Authentication authentication) throws IOException, ServletException {
                //读取cookie中的redirect_url,如果有转向,否则走session中的。
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if (REDIRECT_URL.equals(cookie.getName())) {
                            //cookie.getValue() 经过前端js encodeURIComponent 编码,需要解码
                            try {
                                String decode = URLDecoder.decode(
                                    URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8));
                                response.sendRedirect(decode);
                                return;
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
        handler.setDefaultTargetUrl("/");
        handler.setTargetUrlParameter(REDIRECT_URL);
        handler.setAlwaysUseDefaultTargetUrl(false);
        handler.setUseReferer(false);
        return handler;
    }

    /**
     * 配置票据验证器，用于验证CAS服务端返回的票据。
     */
    @Bean
    public TicketValidator ticketValidator() {
        return new Cas30ServiceTicketValidator(config.getCasServerUrl());
    }

    /**
     * 配置CAS认证提供者，用于处理CAS认证逻辑。
     */
    @Bean
    public CasAuthenticationProvider casAuthenticationProvider(ServiceProperties serviceProperties,
        TicketValidator ticketValidator, AbstractCasAssertionUserDetailsService casUserDetailsService) {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setServiceProperties(serviceProperties);
        provider.setTicketValidator(ticketValidator);
        provider.setAuthenticationUserDetailsService(casUserDetailsService);
        provider.setKey(config.getKey());
        return provider;
    }

    /**
     * 配置安全上下文注销处理器，用于处理注销逻辑。
     */
    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        SecurityContextLogoutHandler handler = new SecurityContextLogoutHandler();
        return handler;
    }

    /**
     * 配置注销过滤器，用于处理用户注销请求。
     */
    @Bean
    public LogoutFilter logoutFilter(SecurityContextLogoutHandler securityContextLogoutHandler) {
        String successUrl = config.getCasUrl() + config.getCasLogoutUri() + "?service=" + config.getSuccessUrl();
        LogoutFilter logoutFilter = new LogoutFilter(successUrl, securityContextLogoutHandler);
        logoutFilter.setFilterProcessesUrl(config.getLogoutUri());
        return logoutFilter;
    }

    /**
     * 配置单点登出过滤器，用于处理CAS单点登出请求。
     */
    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

    /**
     * 配置自定义认证入口点，用于处理认证重定向策略。
     */
    @Bean
    public CustomAuthenticationEntryPoint customAuthRedirectStrategy(CasConfig config) {
        return new CustomAuthenticationEntryPoint(config);
    }

    @Bean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnProperty(prefix = "cas", name = "is-http-security", havingValue = "true")
    public SecurityFilterChain webSecurityConfig(HttpSecurity http, SingleSignOutFilter singleSignOutFilter,
        LogoutFilter logoutFilter,
        CustomAuthenticationEntryPoint authenticationEntryPoint, CasAuthenticationFilter casAuthenticationFilter,
        CasConfig config
    ) throws Exception {
//        return new WebSecurityConfig(singleSignOutFilter, logoutFilter, authenticationEntryPoint,
//            casAuthenticationFilter, config);
        List<String> whilteList = new ArrayList<>(
            List.of(config.getLoginUri(), config.getLogoutUri(),
                config.getCasLogoutUri(), config.getCasLoginUri())
        );
        if (config.getWhileList() != null) {
            whilteList.addAll(Arrays.asList(config.getWhileList()));
        }
        return http.authorizeRequests().antMatchers(whilteList.toArray(new String[0])).permitAll().and()
            .authorizeRequests().anyRequest()
            .authenticated().and().csrf().disable().cors()
            .disable().httpBasic().authenticationEntryPoint(authenticationEntryPoint).and().logout()
            .logoutUrl(config.getLogoutUri()).logoutSuccessUrl(config.getSuccessUrl()).and()
            .addFilter(casAuthenticationFilter)
            .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class)
            .addFilterBefore(logoutFilter, LogoutFilter.class).build();
    }


}
