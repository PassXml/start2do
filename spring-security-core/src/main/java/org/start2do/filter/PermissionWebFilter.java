package org.start2do.filter;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.start2do.Start2doSecurityConfig;
import org.start2do.dto.R;
import org.start2do.entity.security.SysUser;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class PermissionWebFilter extends AbsPermission implements WebFilter {


    public PermissionWebFilter(Start2doSecurityConfig config) {
        super(config);
    }

    // WebFlux 环境下的拦截逻辑
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return handleUnauthenticatedWebFlux(exchange);
        }
        SysUser user = (SysUser) authentication.getPrincipal();
        Set<String> permissions = extractPermissions(user);
        boolean hasPermission = checkPermission(exchange.getRequest().getURI().getPath(), permissions);
        if (!hasPermission) {
            return handleUnauthorizedWebFlux(exchange);
        }
        return chain.filter(exchange);
    }

    // 处理未认证用户 (WebFlux)
    private Mono<Void> handleUnauthenticatedWebFlux(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
            .wrap(R.failed(401, "未登录").toJson().getBytes(StandardCharsets.UTF_8))));
    }

    // 处理无权限用户 (WebFlux)
    private Mono<Void> handleUnauthorizedWebFlux(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(R.failed(500, "权限不足").toJson().getBytes())));
    }
}
