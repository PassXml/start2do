package org.start2do.filter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.start2do.config.PermissionConfig;
import org.start2do.dto.R;
import org.start2do.dto.UserCredentials;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = "start2do.permission",name = "enable",matchIfMissing = true)
public class PermissionWebFilter extends AbsPermission implements WebFilter,IPermission {


    public PermissionWebFilter(PermissionConfig config) {
        super(config);
    }

    // WebFlux 环境下的拦截逻辑
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return chain.filter(exchange);
        }
        UserCredentials user = (UserCredentials) authentication.getPrincipal();
        boolean hasPermission = checkPermission(exchange.getRequest().getURI().getPath(), user,
            extractPermissions(user));
        if (!hasPermission) {
            return handleUnauthorizedWebFlux(exchange);
        }
        return chain.filter(exchange);
    }


    // 处理无权限用户 (WebFlux)
    private Mono<Void> handleUnauthorizedWebFlux(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(R.failed(500, "权限不足").toJson().getBytes())));
    }
}
