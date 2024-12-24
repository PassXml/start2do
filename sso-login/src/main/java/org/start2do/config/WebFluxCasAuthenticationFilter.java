package org.start2do.config;


import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class WebFluxCasAuthenticationFilter implements WebFilter {

    private final CasConfig config;

    /**
     * 计算服务地址，主要是替换url中server的部分，并去除ticket
     *
     * @param context 上下文
     * @param server  服务
     * @return 结果
     */
    public String computeService(ServerHttpRequest request, String server) {
        if (server == null) {
            log.error("getService() argument \"server\" was illegally null.");
            throw new IllegalArgumentException("name of server is required");
        }

        URI uri = request.getURI();

        StringBuilder sb = new StringBuilder(server).append(uri.getPath());

        if (uri.getQuery() != null) {
            String query = uri.getQuery();

            int ticketLoc = query.indexOf("ticket=");
            if (ticketLoc == -1) {
                sb.append("?").append(query);
            } else if (ticketLoc > 0) {
                ticketLoc = query.indexOf("&ticket=");
                if (ticketLoc == -1) {
                    sb.append("?").append(query);
                } else if (ticketLoc > 0) {
                    sb.append("?").append(query, 0, ticketLoc);
                }
            }
        }

        String encodedService = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
        if (log.isTraceEnabled()) {
            log.trace("returning from getService() with encoded service [{}]", encodedService);
        }

        return encodedService;
    }

    /**
     * 核心，跳转cas服务器鉴权
     *
     * @return 结果
     */
    private Mono<Void> redirectToCAS(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return exchange.getSession().map(webSession -> {
            String sessionId = webSession.getId();
            log.trace("entering redirectToCAS()");

            StringBuilder casLoginString = new StringBuilder().append(config.getCasUrl())
                .append(config.getCasLoginUri()).append("?service=").append(config.getSuccessUrl());
//                .append( ? "&renew=true" : "")
//                .append(parameter.casGateway ? "&gateway=true" : "");

//            if (StringUtils.hasText(sessionId)) {
//                String appId = parameter.casServerName + request.getPath().contextPath().value();
//                casLoginString.append("&appId=").append(URLEncoder.encode(appId, StandardCharsets.UTF_8))
//                    .append("&sessionId=").append(sessionId);
//            }

            List<HttpCookie> cookies = request.getCookies().get("JSESSIONID");
            if (!CollectionUtils.isEmpty(cookies)) {
                cookies.stream().filter(Objects::nonNull).map(HttpCookie::getValue)
                    .filter(cookie -> !cookie.equals("null") && !cookie.equals(sessionId))
                    .peek(cookie -> log.debug("Session is timeout. The timeout session is {}", cookie)).findFirst()
                    .ifPresent(cookie -> casLoginString.append("&timeOut=").append(cookie));
            }

            log.debug("Redirecting browser to [{})", casLoginString);
            log.trace("returning from redirectToCAS()");
            CustomServerAuthenticationEntryPoint.RedirectUrl(exchange.getResponse(), casLoginString.toString());
            return Mono.empty();
        }).then();

    }

//
//    private Mono<Void> redirectToInitFailure(CASContext context, String cause) {
//        log.trace("entering redirectToInitFailure()");
//
//        String casLoginString = parameter.casLogin + "?action=initFailure";
//        if (cause != null && cause.equals("Illegal user")) {
//            casLoginString += "&userIllegal=true";
//        }
//
//        String locale = context.getQuery("locale");
//        if (locale != null) {
//            casLoginString += "&locale=" + locale;
//        }
//
//        log.debug("Redirecting browser to [{})", casLoginString);
//        log.trace("returning from redirectToInitFailure()");
//        return context.redirect(casLoginString);
//    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (config.getLoginUri().equals(request.getURI().getPath())) {
            //进行票据鉴权
//            System.out.println(computeService(request, config.getCasUrl()));
            MultiValueMap<String, String> params = request.getQueryParams();
            String ticket = params.getFirst("ticket");
            params.forEach((s, list) -> {
                log.info("{},{}", s, list);
            });
            //有票据,校验票据
            //            if (ticket != null && !ticket.isEmpty()) {
//                return redirectToCAS(exchange);
//            }

        }
        return chain.filter(exchange);
    }
}
