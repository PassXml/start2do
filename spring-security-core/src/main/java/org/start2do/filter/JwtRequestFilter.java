package org.start2do.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.start2do.Start2doSecurityConfig;
import org.start2do.dto.R;
import org.start2do.service.imp.SysLoginUserServiceImpl;
import org.start2do.util.JwtTokenUtil;
import org.start2do.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "jwt", value = "enable")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JwtRequestFilter extends OncePerRequestFilter {

    private final SecurityContextRepository securityContextRepository;
    private final SysLoginUserServiceImpl userService;
    private final Start2doSecurityConfig config;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader(JwtTokenUtil.AUTHORIZATION);
//        if (StringUtils.isEmpty(requestTokenHeader)) {
//            response.setHeader("Content-Type", "application/json;charset=utf-8");
//            response.getWriter().write(R.failed(401, "请重新登录").setError("无权限").toJson());
//            return;
//        }
        String username = null;
        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith(JwtTokenUtil.Bearer)) {
            jwtToken = requestTokenHeader.substring(JwtTokenUtil.BearerLen);
            if ("undefined".equals(jwtToken) || StringUtils.isEmpty(jwtToken)) {
                chain.doFilter(request, response);
                return;
            }
            try {
                username = JwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                if (config.getMockUser() != null && !config.getMockUser()) {
                    log.warn("JWT Token has expired");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                    response.getWriter().write(R.failed(401, "认证失败或者凭证过期").toJson());
                    return;
                }
            }
        }
        if (config.getMockUser() != null && config.getMockUser()) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UserDetails userDetails = userService.loadUserByUsername(config.getMockUserName());
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
        } else if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(username);
            if (JwtTokenUtil.validateToken(jwtToken, userDetails)) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);
            }
        }
        chain.doFilter(request, response);
    }


}
