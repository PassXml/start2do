package org.start2do.ops.aop;

import java.awt.image.TileObserver;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.start2do.dto.PermissionException;
import org.start2do.ops.config.OpsConfig;
import org.start2do.util.TOTPUtil;

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.ops", name = "enable-security", matchIfMissing = true, havingValue = "true")
public class OpsSecurityAop {

    private final OpsConfig opsConfig;

    @Before("execution(* org.start2do.ops.controller..*(..))")
    public void validatePassword(JoinPoint joinPoint) {
        // Get the current HTTP request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String header = request.getHeader("X-TOTP");
        if (StringUtils.isEmpty(header)) {
            throw new PermissionException();
        }
        if (!TOTPUtil.verifyTOTP(opsConfig.getSecret(), header)) {
            throw new PermissionException();
        }
    }
}
