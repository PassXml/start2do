package org.start2do;

import io.ebean.config.CurrentUserProvider;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.start2do.dto.BusinessException;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.entity.business.SysLog;
import org.start2do.entity.business.SysLog.Type;
import org.start2do.service.SysLogService;
import org.start2do.util.StringUtils;
import org.start2do.util.spring.LogAop;
import org.start2do.util.spring.LogAopConfig;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.business.sys-log", value = "enable", havingValue = "true")
public class SysLogAop {

    private final SysLogService sysLogService;
    private final LogAopConfig config;
    public final LogAop.JSON json;
    private final CurrentUserProvider currentUserProvider;

    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    private String getIP(HttpServletRequest request) {
        String[] headers = new String[]{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        return getClientIPByHeader(request, headers);
    }


    private boolean isUnknown(String checkString) {
        return StringUtils.isEmpty(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    private String getMultistageReverseProxyIp(String ip) {
        if (ip != null && ip.indexOf(",") > 0) {
            String[] ips = ip.trim().split(",");
            String[] var2 = ips;
            int var3 = ips.length;
            for (int var4 = 0; var4 < var3; ++var4) {
                String subIp = var2[var4];
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }

        return ip;
    }

    @PostConstruct
    public void init() {
        log.info("启用SysLogAOP");
    }


    private String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        String[] var3 = headerNames;
        int var4 = headerNames.length;

        String ip;
        for (int var5 = 0; var5 < var4; ++var5) {
            String header = var3[var5];
            ip = request.getHeader(header);
            if (!isUnknown(ip)) {
                return getMultistageReverseProxyIp(ip);
            }
        }
        ip = request.getRemoteAddr();
        return getMultistageReverseProxyIp(ip);
    }

    private SysLog getSysLog(HttpServletRequest request) {
        SysLog sysLog = new SysLog();
        sysLog.setType(Type.Info);
        sysLog.setRemoteAddr(getIP(request));
        sysLog.setRequestUri(request.getRequestURI());
        sysLog.setMethod(request.getMethod());
        sysLog.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        StringJoiner params = new StringJoiner("&");
        request.getParameterMap().forEach((s, strings) -> {
            for (String string : strings) {
                params.add(s + "=" + string);
            }
        });
        sysLog.setParams(params.toString());
        StringJoiner joiner = new StringJoiner("");
        Iterator<String> iterator = request.getHeaderNames().asIterator();
        String s = null;
        try {
            while ((s = iterator.next()) != null) {
                joiner.add(s).add("=").add(request.getHeader(s));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        sysLog.setRequestHeader(joiner.toString());
        try {
            sysLog.setRequestBody(new String(request.getInputStream().readAllBytes()));
        } catch (IOException e) {
            sysLog.setResponseBody(e.getMessage());
        }
        return sysLog;
    }

    @Around("@annotation(sysLog)")
    public Object around(ProceedingJoinPoint point, SysLogSetting sysLog) throws Throwable {
        Long startTime = System.currentTimeMillis();
        Object obj = point.proceed();
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
            RequestContextHolder.getRequestAttributes())).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(
            RequestContextHolder.getRequestAttributes())).getResponse();
        String username = String.valueOf(currentUserProvider.currentUser());
        SysLog logVo = getSysLog(request);
        executorService.submit(() -> {
            logVo.setTitle(sysLog.value());
            logVo.setCreatePerson(username);
            logVo.setUpdatePerson(username);
            try {
                logVo.setResponseBody(json.toJson(obj));
            } catch (Throwable e) {
                logVo.setType(Type.Error);
                logVo.setExceptionInfo(e.getMessage());
                throw new BusinessException(e.getMessage());
            } finally {
                Long endTime = System.currentTimeMillis();
                logVo.setUseTime(endTime - startTime);
                if (response != null) {
                    StringJoiner joiner = new StringJoiner("");
                    for (String headerName : response.getHeaderNames()) {
                        joiner.add(headerName).add("=").add(response.getHeader(headerName));
                    }
                    logVo.setResponseHeader(joiner.toString());
                }
                sysLogService.save(logVo);
            }
        });
        return obj;
    }


}
