package org.start2do.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.start2do.Start2doSecurityConfig;
import org.start2do.service.imp.SysLoginUserServiceImpl;
import org.start2do.util.JwtTokenUtil;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ConditionalOnExpression("${jwt.enable:false} && '${spring.main.web-application-type}'==('servlet')")
public class JwtRequestFilter extends OncePerRequestFilter {

    private final SecurityContextRepository securityContextRepository;
    private final SysLoginUserServiceImpl userService;
    private final Start2doSecurityConfig config;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader(JwtTokenUtil.AUTHORIZATION);
        String username = null;
        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith(JwtTokenUtil.Bearer)) {
            jwtToken = requestTokenHeader.substring(JwtTokenUtil.BearerLen);
            try {
                username = JwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.warn("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                if (config.getMockUser() != null && !config.getMockUser()) {
                    log.warn("JWT Token has expired");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
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
