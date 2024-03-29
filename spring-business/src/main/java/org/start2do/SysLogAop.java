package org.start2do;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.start2do.dto.annotation.SysLogSetting;
import org.start2do.entity.business.SysLog;
import org.start2do.entity.business.SysLog.Type;
import org.start2do.service.SysLogService;
import org.start2do.util.StringUtils;
import org.start2do.util.spring.LogAop;
import org.start2do.util.spring.LogAopConfig;

@Aspect
@RequiredArgsConstructor
public class SysLogAop {

    private final SysLogService sysLogService;
    private final LogAopConfig aopConfig;
    public final LogAop.JSON json;

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

    private SysLog getSysLog() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects
            .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
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
        return sysLog;
    }

    @Around("@annotation(sysLog)")
    @SneakyThrows
    public Object around(ProceedingJoinPoint point, SysLogSetting sysLog) {
        SysLog logVo = getSysLog();
        logVo.setTitle(sysLog.value());
        // 发送异步日志事件
        Long startTime = System.currentTimeMillis();
        Object obj;
        try {
            if (json != null) {
                Object[] args = point.getArgs();
                Map<String, String> map = new HashMap<>(args.length);
                for (Object arg : args) {
                    if (aopConfig.getSkinClazz().contains(arg.getClass())) {
                        continue;
                    }
                    map.put(arg.getClass().getSimpleName(), this.json.toJson(arg));
                }
            }
            obj = point.proceed();
        } catch (Exception e) {
            logVo.setType(Type.Error);
            logVo.setExceptionInfo(e.getMessage());
            throw e;
        } finally {
            Long endTime = System.currentTimeMillis();
            logVo.setUseTime(endTime - startTime);
            sysLogService.save(logVo);
        }
        return obj;
    }


}
