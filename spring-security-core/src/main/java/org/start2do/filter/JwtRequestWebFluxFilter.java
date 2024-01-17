package org.start2do.filter;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.start2do.Start2doSecurityConfig;
import org.start2do.dto.UserCredentials;
import org.start2do.service.imp.SysLoginUserServiceImpl;
import org.start2do.util.JwtTokenUtil;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnExpression("${jwt.enable:false}")
public class JwtRequestWebFluxFilter implements WebFilter {

    private final SysLoginUserServiceImpl userService;
    private final Start2doSecurityConfig config;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //保存上下文信息
        if (config.getMockUser() != null && config.getMockUser()) {
            UserCredentials userDetails = userService.loadUserByUsername(config.getMockUserName());
            return chain.filter(exchange).contextWrite(Context.of(
                JwtTokenUtil.AUTHORIZATION, userDetails
            )).contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())));
        } else {
            var jwtFullStr = request.getHeaders().getFirst(JwtTokenUtil.AUTHORIZATION);
            if (jwtFullStr != null && jwtFullStr.startsWith(JwtTokenUtil.Bearer)) {
                String jwtStr = jwtFullStr.substring(JwtTokenUtil.BearerLen);
                try {
                    String username = JwtTokenUtil.getUsernameFromToken(jwtStr);
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    if (Boolean.TRUE.equals(JwtTokenUtil.validateToken(jwtStr, userDetails))) {
                        return chain.filter(exchange).contextWrite(Context.of(
                                JwtTokenUtil.AUTHORIZATION, userDetails
                            )).contextWrite(Context.of(JwtTokenUtil.AUTHORIZATIONStr, jwtStr))
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities())));
                    }
                } catch (ExpiredJwtException e) {

                }
            }
        }
        return chain.filter(exchange);
    }
}
