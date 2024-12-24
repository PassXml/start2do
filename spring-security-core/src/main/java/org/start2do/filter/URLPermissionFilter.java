package org.start2do.filter;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.start2do.dto.IUserInfoGet;
import org.start2do.service.SysUrlPermissionService;


@Slf4j
@RequiredArgsConstructor
public class URLPermissionFilter implements Filter {

    private final SysUrlPermissionService sysUrlPermissionService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        // 获取请求URI
        String requestUri = httpServletRequest.getRequestURI();
        log.debug("请求访问,{}", requestUri);
        // 继续处理请求
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof IUserInfoGet) {
            IUserInfoGet userInfo = (IUserInfoGet) principal;
            Collection<String> roles = userInfo.getRoles_();
            sysUrlPermissionService.findAllByRolesOrUserId(roles, userInfo.getUserId());
        }
        chain.doFilter(request, response);
    }
}
