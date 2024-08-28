package org.start2do.util.spring;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

@UtilityClass
public class IPUtil {

    public static String getRealRequestIp(HttpServletRequest request) {
        String ip = null;
        for (String key : strings) {
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

    private String[] strings = new String[]{
        "x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    };

    public static String getRealRequestIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = null;
        for (String key : strings) {
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
}
