package org.start2do.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.start2do.Start2doSecurityConfig;
import org.start2do.entity.security.SysUser;

@Component
@ConditionalOnWebApplication(type = Type.SERVLET)
public class PermissionInterceptor extends AbsPermission implements HandlerInterceptor {


    public PermissionInterceptor(Start2doSecurityConfig config) {
        super(config);
    }

    // Servlet 环境下的拦截逻辑
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return handleUnauthenticated(response);
        }

        SysUser user = (SysUser) authentication.getPrincipal();
        Set<String> permissions = extractPermissions(user);
        boolean hasPermission = checkPermission(request.getRequestURI(), permissions);

        if (!hasPermission) {
            return handleUnauthorized(response);
        }
        return true;
    }

    // 处理未认证用户 (Servlet)
    private boolean handleUnauthenticated(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("用户未认证");
        return false;
    }

    // 处理无权限用户 (Servlet)
    private boolean handleUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("访问被拒绝：权限不足");
        return false;
    }
}
