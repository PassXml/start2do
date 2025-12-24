package org.start2do.plugin.server.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.start2do.plugin.config.PluginSystemProperties;

/**
 * 认证拦截器
 * <p>
 * 验证请求头中的 X-Auth-Token 是否与配置的 token 匹配
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "X-Auth-Token";

    private final PluginSystemProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果未启用认证，直接放行
        if (!properties.getServer().getAuth().isEnabled()) {
            return true;
        }

        // 获取配置的 token
        String configuredToken = properties.getServer().getAuth().getToken();
        if (!StringUtils.hasText(configuredToken)) {
            log.warn("Auth is enabled but token is not configured, allowing request");
            return true;
        }

        // 获取请求头中的 token
        String requestToken = request.getHeader(AUTH_HEADER);

        // 验证 token
        if (!StringUtils.hasText(requestToken) || !configuredToken.equals(requestToken)) {
            log.warn("Authentication failed: invalid or missing token. Remote IP: {}, URI: {}",
                request.getRemoteAddr(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized: Invalid or missing authentication token\"}");
            return false;
        }

        // 认证成功
        return true;
    }
}
