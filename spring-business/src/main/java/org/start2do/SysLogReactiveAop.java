package org.start2do;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.entity.business.SysLog;
import org.start2do.service.webflux.SysLogReactiveService;
import org.start2do.util.spring.LogAop;
import org.start2do.util.spring.LogAopConfig;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.sys-log", value = "enable", havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SysLogReactiveAop {

    private final SysLogReactiveService logReactiveService;
    private final LogAopConfig config;
    public final LogAop.JSON json;


    //获取当前请求对象
    public static Mono<Optional<SysLog>> getLog() {
        return Mono.deferContextual(contextView -> {
            Optional<SysLog> optional = contextView.getOrEmpty(SysLog.class);
            return Mono.just(optional);
        });
    }


    public static String getHeader(HttpHeaders headers) {
        StringJoiner joiner = new StringJoiner("");
        Set<String> keySet = headers.keySet();
        try {
            for (String headerKey : keySet) {
                for (String headerValue : headers.get(headerKey)) {
                    joiner.add(headerKey).add("=").add(headerValue);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return joiner.toString();
    }

    @PostConstruct
    public void init() {
        log.info("启用SysLogAOP");
    }

    @Around("@annotation(sysLog)")
    public Mono<Object> around(ProceedingJoinPoint point, SysLogSetting sysLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        return getLog().zipWith((Mono<Object>) point.proceed()).flatMap(objects -> {
            Object result = objects.getT2();
            Optional<SysLog> optional = objects.getT1();
            return optional.map(value -> {
                value.setUseTime(System.currentTimeMillis() - startTime);
                value.setTitle(sysLog.value());
                boolean skip = false;
                for (Class clazz : config.getSkinClazz()) {
                    //判断返回值是class是否是clazz类或子类
                    if (clazz.isAssignableFrom(result.getClass())) {
                        skip = true;
                        break;
                    }
                }
                if (!skip) {
                    try {
                        value.setResponseBody(json.toJson(result));
                    } catch (Exception e) {
                        value.setExceptionInfo(e.getMessage());
                    }
                }
                return logReactiveService.save(value).map(ctx -> result)
                    .onErrorResume(throwable -> {
                        log.error(throwable.getMessage(), throwable);
                        return Mono.just(result);
                    });
            }).orElseGet(() -> Mono.just(result));
        });
    }


}
