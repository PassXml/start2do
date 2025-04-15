package org.start2do.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final CasConfig config;

    public static void RedirectUrl(ServerHttpResponse response, String redirectUrl) {
        response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        response.getHeaders().put("Location", List.of(redirectUrl));
    }

    private void move301(ServerHttpResponse response, String redirectUrl) {
        RedirectUrl(response,
            config.getCasUrl() + config.getCasLoginUri() + "?service=" + config.getBaseUrl() + config.getLoginUri()
            + "&" + CustomAuthenticationEntryPoint.REDIRECT_URL + "=" + redirectUrl);
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        if (ex != null) {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            if ("/".equals(request.getURI().getPath())) {
                String redirectUrl = request.getQueryParams().getFirst(CustomAuthenticationEntryPoint.REDIRECT_URL);
                //redirectUrl 不为空
                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    return exchange.getSession().map(webSession -> {
                        webSession.getAttributes().put("SPRING_SECURITY_SAVED_REQUEST", redirectUrl);
                        move301(response, redirectUrl);
                        return webSession;
                    }).then();
                } else {
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    for (Entry<String, List<HttpCookie>> entry : cookies.entrySet()) {
                        String key = entry.getKey();
                        List<HttpCookie> httpCookies = entry.getValue();
                        if (CustomAuthenticationEntryPoint.REDIRECT_URL.equals(key)) {
                            if (httpCookies.isEmpty()) {
                                break;
                            }
                            HttpCookie cookie = httpCookies.get(0);
                            move301(response,
                                config.getCasUrl() + config.getCasLoginUri() + "?service=" + config.getBaseUrl()
                                + config.getLoginUri() + "&" + CustomAuthenticationEntryPoint.REDIRECT_URL + "="
                                + cookie.getValue());
                            return Mono.empty();
                        }
                    }
                }
                move301(response, config.getCasUrl() + config.getCasLoginUri() + "?service=" + config.getBaseUrl()
                                  + config.getLoginUri());
                return Mono.empty();
            } else {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().put("content-type", List.of("application/json;utf-8"));
                return response.writeWith(
                    Mono.just(response.bufferFactory().wrap("{\"code\":401}".getBytes(StandardCharsets.UTF_8))));
            }
        }
        return Mono.empty();
    }
}
