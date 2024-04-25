package org.start2do.filter;

import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.start2do.SysLogReactiveAop;
import org.start2do.config.SysLogUrlPatternService;
import org.start2do.entity.business.SysLog;
import org.start2do.entity.business.SysLog.Type;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.sys-log", value = "enable", havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SysLogRequestWebFilter implements WebFilter {

    private final SysLogUrlPatternService urlPatternService;

    private String getIP(ServerHttpRequest request) {
        String[] headers = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        return getClientIPByHeader(request, headers);
    }


    private boolean isUnknown(String checkString) {
        return StringUtils.isEmpty(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    private String getMultistageReverseProxyIp(String ip) {
        if (ip != null && ip.indexOf(",") > 0) {
            String[] ips = ip.trim().split(",");
            String[] var2 = ips;
            int var3 = ips.length;
            for (int var4 = 0; var4 < var3; ++var4) {
                String subIp = var2[var4];
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }


    private String getClientIPByHeader(ServerHttpRequest request, String... headerNames) {
        String[] var3 = headerNames;
        int var4 = headerNames.length;

        String ip;
        for (int var5 = 0; var5 < var4; ++var5) {
            String header = var3[var5];
            ip = request.getHeaders().getFirst(header);
            if (!isUnknown(ip)) {
                return getMultistageReverseProxyIp(ip);
            }
        }
        ip = request.getRemoteAddress().toString();
        return getMultistageReverseProxyIp(ip);
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!ReReadRequestBodyFilter.isHandle(request) || urlPatternService.isMatch(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        HttpMethod method = request.getMethod();
        HttpHeaders headers = request.getHeaders();
        SysLog sysLog = new SysLog();
        sysLog.setType(Type.Info);
        sysLog.setRemoteAddr(getIP(request));
        sysLog.setRequestUri(request.getPath().toString());
        sysLog.setMethod(method.name());
        sysLog.setUserAgent(headers.getFirst(HttpHeaders.USER_AGENT));
        StringJoiner params = new StringJoiner("&");
        request.getQueryParams().forEach((s, strings) -> {
            for (String string : strings) {
                params.add(s + "=" + string);
            }
        });
        sysLog.setParams(params.toString());
        sysLog.setRequestHeader(SysLogReactiveAop.getHeader(headers));
        try {
            String jwtStr = headers.getFirst(JwtTokenUtil.AUTHORIZATION).substring(JwtTokenUtil.BearerLen);
            String username = JwtTokenUtil.getUsernameFromToken(jwtStr);
            sysLog.setUpdatePerson(username);
            sysLog.setCreatePerson(username);
        } catch (Exception e) {
            sysLog.setExceptionInfo(e.getMessage());
        }
        return DataBufferUtils.join(request.getBody()).map(dataBuffer -> {
            int byteCount = dataBuffer.readableByteCount();
            if (byteCount == 0) {
                return true;
            }
            byte[] bytes = new byte[byteCount];
            dataBuffer.read(bytes, 0, byteCount);
            sysLog.setRequestBody(new String(bytes));
            return true;
        }).then(chain.filter(exchange).contextWrite(Context.of(
            SysLog.class, sysLog)
        ));
    }
}
