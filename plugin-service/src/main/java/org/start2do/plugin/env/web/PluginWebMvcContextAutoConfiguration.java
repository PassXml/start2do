package org.start2do.plugin.env.web;

import lombok.RequiredArgsConstructor;
import org.pf4j.PluginManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 仅在 Spring MVC 存在时启用插件请求上下文绑定。
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnClass(WebMvcConfigurer.class)
@ConditionalOnBean(PluginManager.class)
public class PluginWebMvcContextAutoConfiguration implements WebMvcConfigurer {

    private final PluginManager pluginManager;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PluginRequestContextInterceptor(pluginManager));
    }
}

