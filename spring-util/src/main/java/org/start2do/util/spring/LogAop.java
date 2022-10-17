package org.start2do.util.spring;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.StringJoiner;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author ：chicunxiang
 * @date ：Created in 2022/3/25 11:10
 * @description：
 * @version: 1.0
 */

public abstract class LogAop {

    protected final Logger log = LoggerFactory.getLogger(getName());

    public abstract String getName();

    protected JSON jsonUtils;
    protected static List<Class> skinClass = new ArrayList<>(
        Arrays.asList(
            HttpServletRequest.class, HttpServletResponse.class,
            ServletResponse.class, ServletRequest.class, OutputStream.class,
            ByteArrayOutputStream.class
        )
    );

//    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
//    public void controller() {
//
//    }

//    @Around("controller()")
    public Object before(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
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
                for (Class aClass : skinClass) {
                    if (arg == null || aClass.equals(arg.getClass())) {
                        skin = true;
                        break;
                    }
                }
                if (skin) {
                    continue;
                }
                body.add(jsonUtils.toJson(arg));
            }
            Object proceed = point.proceed();
            response = jsonUtils.toJson(proceed);
            return proceed;
        } catch (Exception e) {
            response = e.getMessage();
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("请求IP: {} 请求URL :{} - {} ,请求头 :{}, 请求参数 :{} , 返回结果 :{}, 响应时间 :{}",
                request.getRemoteAddr(),
                request.getMethod(), requestURI, headerString, body, response, endTime - startTime);
        }
    }

    public interface JSON {

        String toJson(Object object);
    }
}
