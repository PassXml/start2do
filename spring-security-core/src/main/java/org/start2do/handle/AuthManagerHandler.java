package org.start2do.handle;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.start2do.service.reactive.SysPermissionReactiveService;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
public class AuthManagerHandler implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final SysPermissionReactiveService permissionReactiveService;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        //        permissionReactiveService.findOne(new QSysPermission().users.id.eq());
        ServerHttpRequest request = object.getExchange().getRequest();
        String requestUrl = request.getPath().pathWithinApplication().value();
        return Mono.just(new AuthorizationDecision(false));
    }
}
