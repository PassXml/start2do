package org.start2do.util.spring;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.start2do.util.spring.dto.JSON;
import org.start2do.util.spring.dto.LogSetting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author lijie
 */

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.log", value = "enable", havingValue = "true")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class LogAopReactive {

    private Logger log;
    private final JSON jsonUtils;

    private final LogAopConfig logAopConfig;

    @PostConstruct
    public void init() {
        log = LoggerFactory.getLogger(logAopConfig.getName());
        log.info("启用LogAOP");
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public Object before(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        LogSetting logSetting = method.getAnnotation(LogSetting.class);
        if (logSetting != null && logSetting.ignore()) {
            return point.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object proceed = point.proceed();

        if (proceed instanceof Mono) {
            return ((Mono<?>) proceed).doOnNext(result -> logRequest(point, startTime, result).subscribe());
        } else if (proceed instanceof Flux) {
            return ((Flux<?>) proceed).doOnNext(result -> logRequest(point, startTime, result).subscribe());
        } else {
            logRequest(point, startTime, proceed).subscribe();
            return proceed;
        }
    }

    private Mono<Boolean> logRequest(ProceedingJoinPoint point, long startTime, Object result) {
        return getCurrentExchange().flatMap(exchange -> {
            String requestURI = exchange.getRequest().getURI().getPath();
            if (logAopConfig.getSkipUrl().contains(requestURI)) {
                return Mono.empty();
            }
            StringJoiner headerString = new StringJoiner(",");
            exchange.getRequest().getHeaders()
                .forEach((key, value) -> headerString.add(String.join(":", key, String.join(",", value))));

            String response = "";
            StringJoiner body = new StringJoiner(",");
            Exception error = null;
            try {
                Object[] args = point.getArgs();
                for (Object arg : args) {
                    Boolean skip = false;
                    for (Class<?> aClass : logAopConfig.getSkinClazz()) {
                        if (arg == null || aClass.isAssignableFrom(arg.getClass())) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) {
                        continue;
                    }
                    body.add(jsonUtils.toJson(arg));
                }
                response = jsonUtils.toJson(result);
            } catch (Exception e) {
                response = e.getMessage();
                error = e;
            } finally {
                long endTime = System.currentTimeMillis();
                log.info("请求IP: {} 请求URL :{} - {} ,请求头 :{}, 请求参数 :{} , 返回结果 :{}, 响应时间 :{}",
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                    exchange.getRequest().getMethod(), requestURI, headerString, body, response,
                    endTime - startTime);
            }
            if (error != null) {
                return Mono.error(error);
            }
            return Mono.just(true);
        });
    }

    private Mono<ServerWebExchange> getCurrentExchange() {
        return ReactiveRequestContextHolder.getContext()
            .filter(map -> map.containsKey(ServerWebExchange.class.getName()))
            .map(context -> (ServerWebExchange) context.get(ServerWebExchange.class.getName()));
    }


}
