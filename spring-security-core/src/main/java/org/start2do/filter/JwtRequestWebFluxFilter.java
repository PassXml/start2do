package org.start2do.filter;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.start2do.Start2doSecurityConfig;
import org.start2do.dto.R;
import org.start2do.dto.UserCredentials;
import org.start2do.service.imp.SysLoginUserReactiveServiceImpl;
import org.start2do.util.JwtTokenUtil;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnExpression("${jwt.enable:false}")
public class JwtRequestWebFluxFilter implements WebFilter {

    private final SysLoginUserReactiveServiceImpl userService;
    private final Start2doSecurityConfig config;

    private final CustomContextInfo customContextInfo;
    private static String mockJwtStr;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //保存上下文信息
        if (config.getMockUser() != null && config.getMockUser()) {
            return customContextInfo.loadUserBefore(userService.findByUsername(config.getMockUserName()))
                .zipWith(customContextInfo.injectOtherInfo(null)).flatMap(
                    objects -> {
                        UserDetails userDetails = objects.getT1();
                        if (mockJwtStr == null) {
                            mockJwtStr = JwtTokenUtil.generateToken(
                                new UserCredentials(userDetails, config.getMockUserId())
                            );
                        }
                        return chain.filter(exchange).contextWrite(Context.of(JwtTokenUtil.AUTHORIZATION, userDetails))
                            .contextWrite(Context.of(JwtTokenUtil.AUTHORIZATIONStr, mockJwtStr))
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities())))
                            .contextWrite(
                                context -> customContextInfo.injectContext(context, null, config.getTenantId(),
                                    objects.getT2()));
                    }
                );
        } else {
            var jwtFullStr = request.getHeaders().getFirst(JwtTokenUtil.AUTHORIZATION);
            if (jwtFullStr != null && jwtFullStr.startsWith(JwtTokenUtil.Bearer)) {
                String jwtStr = jwtFullStr.substring(JwtTokenUtil.BearerLen);
                try {
                    String username = JwtTokenUtil.getUsernameFromToken(jwtStr);
                    return customContextInfo.loadUserBefore(userService.findByUsername(username))
                        .filter(userDetails -> JwtTokenUtil.validateToken(jwtStr, userDetails))
                        .zipWith(customContextInfo.injectOtherInfo(jwtStr)).flatMap(
                            objects -> {
                                UserDetails userDetails = objects.getT1();
                                return chain.filter(exchange)
                                    .contextWrite(Context.of(JwtTokenUtil.AUTHORIZATION, userDetails))
                                    .contextWrite(Context.of(JwtTokenUtil.AUTHORIZATIONStr, jwtStr)).contextWrite(
                                        ReactiveSecurityContextHolder.withAuthentication(
                                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                                userDetails.getAuthorities())))
                                    .contextWrite(context -> customContextInfo.injectContext(context, jwtStr, null,
                                        objects.getT2()));
                            }
                        );
                } catch (JwtException e) {
                    exchange.getResponse().setRawStatusCode(401);
                    exchange.getResponse().writeWith(Mono.just(
                        exchange.getResponse().bufferFactory()
                            .wrap(R.failed(401, e.getMessage()).toJson().getBytes())));
                    return Mono.empty();
                }
            }
        }
        return chain.filter(exchange);
    }

    @Bean
    @ConditionalOnMissingBean(CustomContextInfo.class)
    public CustomContextInfo customContextInfo() {
        return new CustomContextInfo() {

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

    public interface CustomContextInfo {

        <R> Mono<R> loadUserBefore(Mono<R> mono);

        Context injectContext(Context context, String jwtStr, Integer tenantId, Object otherInfo);

        Mono<Object> injectOtherInfo(String jwtStr);
    }
}
