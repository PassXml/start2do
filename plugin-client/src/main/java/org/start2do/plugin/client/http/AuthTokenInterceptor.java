package org.start2do.plugin.client.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.start2do.plugin.client.config.PluginClientProperties;

import java.io.IOException;

/**
 * 认证令牌拦截器
 * <p>
 * 在每个发往 plugin-server 的请求中自动添加 X-Auth-Token 请求头
 */
@Slf4j
@RequiredArgsConstructor
public class AuthTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final String AUTH_HEADER = "X-Auth-Token";

    private final PluginClientProperties properties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 如果配置了认证令牌，则添加到请求头
        String authToken = properties.getAuthToken();
        if (StringUtils.hasText(authToken)) {
            request.getHeaders().set(AUTH_HEADER, authToken);
            log.debug("Added auth token to request: {} {}", request.getMethod(), request.getURI());
        }
        return execution.execute(request, body);
    }
}
