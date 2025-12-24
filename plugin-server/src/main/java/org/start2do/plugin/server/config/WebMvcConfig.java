package org.start2do.plugin.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.start2do.plugin.server.web.interceptor.AuthInterceptor;

/**
 * WebMvc 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册认证拦截器：仅拦截本模块实际对外暴露的 API 路径，避免影响同进程内其他模块的 /api/**。
        registry.addInterceptor(authInterceptor)
            .addPathPatterns(
                "/api/nodes",
                "/api/nodes/**",
                "/api/plugins/server",
                "/api/plugins/server/**"
            )
            .order(1);
    }
}
