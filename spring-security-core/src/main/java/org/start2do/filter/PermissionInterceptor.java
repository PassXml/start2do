package org.start2do.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.start2do.config.PermissionConfig;
import org.start2do.dto.R;
import org.start2do.dto.UserCredentials;

@Component
@ConditionalOnProperty(prefix = "start2do.permission", name = "enable", matchIfMissing = true)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class PermissionInterceptor extends AbsPermission implements Filter, IPermission {

  public PermissionInterceptor(PermissionConfig config) {
    super(config);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // 初始化逻辑，如果有需要
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      chain.doFilter(request, response);
      return;
    }
    UserCredentials user = (UserCredentials) authentication.getPrincipal();
    boolean hasPermission =
        checkPermission(httpRequest.getRequestURI(), user, extractPermissions(user));
    if (!hasPermission) {
      handleUnauthorized(httpResponse);
      return;
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // 销毁逻辑，如果有需要
  }

  // 处理无权限用户
  private void handleUnauthorized(HttpServletResponse response) throws IOException {
    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write(R.failed(500, "权限不足").toJson());
  }
}
