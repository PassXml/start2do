package org.start2do.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.PathContainer;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.util.spring.LogAopConfig;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SysLogUrlPatternService implements CommandLineRunner {

    @Getter
    private Set<PathPattern> url = new HashSet<>();
    private Set<String> filterUrl = new HashSet<>();
    private final LogAopConfig config;
    @Resource
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final LoadingCache<String, Boolean> cache = Caffeine.newBuilder()
        .maximumSize(50_000)
        .expireAfterAccess(Duration.ofMinutes(5))
        .build(urlStr -> {
            for (PathPattern pathPattern : this.url) {
                if (pathPattern.matches(PathContainer.parsePath(urlStr))) {
                    return true;
                }
            }
            return false;

        });

    @Override
    public void run(String... args) throws Exception {
        // 获取url与类和方法的对应信息
        Map<org.springframework.web.reactive.result.method.RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        for (org.springframework.web.reactive.result.method.RequestMappingInfo info : map.keySet()) {
            // 获取url的Set集合，一个方法可能对应多个url
            HandlerMethod method = map.get(info);
            SysLogSetting setting = method.getMethodAnnotation(SysLogSetting.class);
            if (setting == null) {
                continue;
            }
            Set<PathPattern> patterns = info.getPatternsCondition().getPatterns();
            for (PathPattern pattern : patterns) {
                filterUrl.add(pattern.toString());
                url.add(pattern);
            }
        }
        for (String string : config.getSkipUrl()) {
            if (filterUrl.contains(string)) {
                continue;
            }
            url.add(PathPatternParser.defaultInstance.parse(string));
        }
        filterUrl.clear();
        filterUrl = null;
    }

    public Boolean isMatch(String url) {
        return cache.get(url);
    }
}
