package org.start2do;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class BaseExceptionHandler {

    protected final SpringCommonConfig config;

    protected void log(Exception e) {
        String uri = getRequestUri();
        if (config.getErrorTrace() == null) {
            log.error("请求URI: {}, 错误信息: {}", uri, e.getMessage(), e);
        } else {
            log.error("请求URI: {}, 错误信息: {}", uri, e.getMessage(), e);
            for (StackTraceElement element : e.getStackTrace()) {
                if (element.getClassName().startsWith(config.getErrorTrace().getPackageName())) {
                    log.info(
                        "请求URI: {}, 错误信息: {}, 类: {}, 方法: {}, 行号: {}",
                        uri,
                        e.getMessage(),
                        element.getClassName(),
                        element.getMethodName(),
                        element.getLineNumber());
                    break;
                }
            }
        }
    }

    // 获取请求URI，支持Servlet和WebFlux
    protected String getRequestUri() {
        try {
            // 尝试获取Servlet环境下的URI
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes) {
                ServletRequestAttributes servletAttributes = (ServletRequestAttributes) attributes;
                return servletAttributes.getRequest().getRequestURI();
            }
        } catch (Exception e) {
            log.warn("无法从Servlet环境获取URI: {}", e.getMessage());
        }
        // 如果不是Servlet环境，尝试WebFlux环境
        try {
            return Mono.deferContextual(
                    contextView -> {
                        ServerWebExchange exchange = contextView.get(ServerWebExchange.class);
                        return Mono.just(exchange.getRequest().getURI().toString());
                    })
                .blockOptional()
                .orElse("未知URI (WebFlux环境未获取到)");
        } catch (Exception e) {
            log.warn("无法从WebFlux环境获取URI: {}", e.getMessage());
        }
        return "未知URI";
    }

    // 在 ExceptionHandler 类中添加新方法或修改现有方法
    protected void logRequestBody() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes) {
                ServletRequestAttributes servletAttributes = (ServletRequestAttributes) attributes;
                HttpServletRequest request = servletAttributes.getRequest();

                // 记录请求头信息
                Enumeration<String> headerNames = request.getHeaderNames();
                StringBuilder headers = new StringBuilder();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    headers
                        .append(headerName)
                        .append(": ")
                        .append(request.getHeader(headerName))
                        .append("\n");
                }

                // 尝试读取请求体
                // 注意：这可能需要包装HttpServletRequest以允许多次读取请求体
                String requestBody = "";
                try {
                    requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("无法读取请求体: {}", e.getMessage());
                }

                log.info(
                    "请求URI: {}, 请求方法: {}, 请求头: \n{}, 请求体: {}",
                    request.getRequestURI(),
                    request.getMethod(),
                    headers.toString(),
                    requestBody);
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败: {}", e.getMessage());
        }
    }

}
