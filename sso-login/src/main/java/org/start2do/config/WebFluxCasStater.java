package org.start2do.config;


import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

/**
 * CAS单点登录配置类，当配置文件中cas.enable属性为true时生效。
 */
@Import(CasConfig.class)
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(value = "cas.enable", havingValue = "true")
public class WebFluxCasStater {

    private final CasConfig casConfig;

    private ServerAuthenticationSuccessHandler loginSuccessHandler(String uri) {
        RedirectServerAuthenticationSuccessHandler successHandler = new RedirectServerAuthenticationSuccessHandler();
        successHandler.setLocation(URI.create(uri));
        return successHandler;
    }

    private ServerLogoutSuccessHandler logoutSuccessHandler(String uri) {
        RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
        successHandler.setLogoutSuccessUrl(URI.create(uri));
        return successHandler;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChainSecure(ServerHttpSecurity http,
        ReactiveAuthenticationManager reactiveAuthenticationManager) {
        List<String> whilteList = new ArrayList<>(
            List.of(casConfig.getLoginUri(), casConfig.getLogoutUri(),
                casConfig.getCasLogoutUri(), casConfig.getCasLoginUri())
        );
        if (casConfig.getWhileList() != null) {
            whilteList.addAll(Arrays.asList(casConfig.getWhileList()));
        }
        return http
            .authenticationManager(reactiveAuthenticationManager)
            .authorizeExchange(
                (authorizeExchange) -> {
                    authorizeExchange.pathMatchers(
                            whilteList.toArray(new String[0])
                        )
                        .permitAll()
                        .anyExchange()
                        .authenticated();
                })
            .csrf().disable().cors().disable()
            .httpBasic().authenticationEntryPoint(new CustomServerAuthenticationEntryPoint(casConfig))
            .and()
            .formLogin().disable()
            .addFilterBefore(new WebFluxCasAuthenticationFilter(casConfig), SecurityWebFiltersOrder.AUTHENTICATION)
            .logout((logout) -> logout.logoutUrl(casConfig.getLoginUri()))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .build();
    }


    /**
     * 配置服务属性，用于CAS认证过程中的服务标识。
     */
    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casConfig.getBaseUrl() + casConfig.getLoginUri());
        //默认为false,但如果您的应用程序特别敏感，则应设置为 true。该参数的作用是告诉 CAS 登录服务，单点登录是不可接受的。用户需要重新输入用户名和密码才能访问服务。
        serviceProperties.setSendRenew(false);
        //验证所有的请求,而不是只认证/login/cas的请求
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> User.withUsername(username).password("{noop}password").roles("USER").build();
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveAuthenticationManager.class)
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return new ReactiveAuthenticationManager() {
            @Override
            public Mono<Authentication> authenticate(Authentication authentication) {
                boolean authenticated = authentication.isAuthenticated();
                System.out.println("用户认证结果:" + authenticated);

                return Mono.empty();
            }
        };
    }

}
