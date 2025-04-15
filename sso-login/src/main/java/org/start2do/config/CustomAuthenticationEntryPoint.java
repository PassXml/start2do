package org.start2do.config;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;


public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CasConfig config;
    /**
     * 重定向的URL
     */
    public static final String REDIRECT_URL = "redirect_url";

    public CustomAuthenticationEntryPoint(CasConfig config) {
        this.config = config;
    }

    /**
     * 当用户未认证时，重定向到CAS登录页面。
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        if ("/".equals(request.getRequestURI()) || "".equals(request.getRequestURI())) {
            String redirectUrl = request.getParameter(REDIRECT_URL);
            //redirectUrl 不为空
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                request.setAttribute("SPRING_SECURITY_SAVED_REQUEST",
                    new DefaultSavedRequest(request, ServletRequest::getServerPort));
                response.sendRedirect(
                    config.getCasUrl() + config.getCasLoginUri() + "?service=" + config.getBaseUrl()
                    + config.getLoginUri() + "&" + REDIRECT_URL + "=" + redirectUrl);
                return;
            } else {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (REDIRECT_URL.equals(cookie.getName())) {
                            response.sendRedirect(
                                config.getCasUrl() + config.getCasLoginUri() + "?service=" + config.getBaseUrl()
                                + config.getLoginUri() + "&" + REDIRECT_URL + "=" + cookie.getValue());
                            return;
                        }
                    }
                }
            }
            response.sendRedirect(config.getCasUrl() + config.getCasLoginUri() + "?service=" + config.getBaseUrl()
                                  + config.getLoginUri());
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("content-type", "application/json;utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println("{\"code\":401}");
        out.flush();
        out.close();
    }
}
