package org.start2do.util.spring;

import jakarta.annotation.PostConstruct;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

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
            return ((Mono<?>) proceed).doOnNext(result -> logRequest(point, startTime, result));
        } else if (proceed instanceof Flux) {
            return ((Flux<?>) proceed).doOnNext(result -> logRequest(point, startTime, result));
        } else {
            logRequest(point, startTime, proceed);
            return proceed;
        }
    }

    private void logRequest(ProceedingJoinPoint point, long startTime, Object result) {
        executorService.submit(() -> {
            try {
                ServerWebExchange exchange = getCurrentExchange();
                if (exchange != null) {
                    String requestURI = exchange.getRequest().getURI().getPath();
                    if (logAopConfig.getSkipUrl().contains(requestURI)) {
                        return;
                    }
                    StringJoiner headerString = new StringJoiner(",");
                    exchange.getRequest().getHeaders().forEach((key, value) ->
                        headerString.add(String.join(":", key, String.join(",", value))));

                    String response = "";
                    StringJoiner body = new StringJoiner(",");
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
                        throw e;
                    } finally {
                        long endTime = System.currentTimeMillis();
                        log.info("请求IP: {} 请求URL :{} - {} ,请求头 :{}, 请求参数 :{} , 返回结果 :{}, 响应时间 :{}",
                            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                            exchange.getRequest().getMethod(),
                            requestURI,
                            headerString,
                            body,
                            response,
                            endTime - startTime);
                    }
                }
            } catch (Exception e) {
                log.error("日志记录失败", e);
            }
        });
    }

    private ServerWebExchange getCurrentExchange() {
        return ReactiveRequestContextHolder.getContext()
            .map(context -> (ServerWebExchange) context.get(ServerWebExchange.class.getName()))
            .block();
    }

    public interface JSON {

        String toJson(Object object);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogSetting {

        boolean ignore() default false;
    }

}
