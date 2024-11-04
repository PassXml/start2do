package org.start2do.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

@UtilityClass
public class HttpHeaderUtil {

    public String getUserAgent(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(HttpHeaders.USER_AGENT);
    }

    public static String getRealRequestIp(HttpServletRequest request) {
        String ip = null;
        for (String key : Headers) {
            ip = request.getHeader(key);
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                continue;
            }
            break;
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (null != ip && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    private Optional<String> getHeader(HttpHeaders headers, String header) {
        return Optional.ofNullable(headers.get(header)).map(List::stream).map(Stream::findFirst)
            .map(Optional::get);
    }

    private String[] Headers = new String[]{
        "x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    };

    public static String getRealRequestIp(org.springframework.http.server.reactive.ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = null;
        for (String key : Headers) {
            Optional<String> optional = getHeader(headers, key);
            if (optional.isPresent()) {
                ip = optional.get();
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    continue;
                }
                break;
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().toString();
        }
        if (null != ip && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}
