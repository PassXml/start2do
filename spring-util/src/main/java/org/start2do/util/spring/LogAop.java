package org.start2do.util.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author ：chicunxiang
 * @date ：Created in 2022/3/25 11:10
 * @description：
 * @version: 1.0
 */

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.log", value = "enable", havingValue = "true")
public class LogAop {

    private Logger log;
    private final JSON jsonUtils;

    private final LogAopConfig logAopConfig;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    @PostConstruct
    public void init() {
        log = LoggerFactory.getLogger(logAopConfig.getName());
        log.info("启用LogAOP");
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public void controller() {

    }

    @Around("controller()")
    public Object before(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        LogSetting logSetting = method.getAnnotation(LogSetting.class);
        if (logSetting != null) {
            if (logSetting.ignore()) {
                return point.proceed();
            }
        }
        Object proceed = point.proceed();
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (sra != null) {
            HttpServletRequest request = sra.getRequest();
            executorService.submit(() -> {
                String requestURI = request.getRequestURI();
                StringJoiner headerString = new StringJoiner(",");
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    headerString.add(String.join(":", headerName, request.getHeader(headerName)));
                }
                String response = "";
                StringJoiner body = new StringJoiner(",");
                try {
                    Object[] args = point.getArgs();
                    for (Object arg : args) {
                        Boolean skin = false;
                        for (Class aClass : logAopConfig.getSkinClazz()) {
                            if (arg == null || aClass.isAssignableFrom(arg.getClass())) {
                                skin = true;
                                break;
                            }
                        }
                        if (skin) {
                            continue;
                        }
                        body.add(jsonUtils.toJson(arg));
                    }
                    response = jsonUtils.toJson(proceed);
                } catch (Exception e) {
                    response = e.getMessage();
                    throw e;
                } finally {
                    long endTime = System.currentTimeMillis();
                    log.info("请求IP: {} 请求URL :{} - {} ,请求头 :{}, 请求参数 :{} , 返回结果 :{}, 响应时间 :{}",
                        request.getRemoteAddr(), request.getMethod(), requestURI, headerString, body, response,
                        endTime - startTime);
                }
            });
        }

        return proceed;
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
