package org.start2do.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.start2do.BaseExceptionHandler;
import org.start2do.SpringCommonConfig;
import org.start2do.constant.ErrorConstant;
import org.start2do.dto.R;

@Slf4j
@ControllerAdvice
@ConditionalOnProperty(prefix = "start2do", name = "enable-exception", matchIfMissing = true, havingValue = "true")
public class AuthExceptionHandle extends BaseExceptionHandler {

    public AuthExceptionHandle(SpringCommonConfig config) {
        super(config);
        log.info("AuthExceptionHandle 初始化");
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
    public R accessDeniedException(AccessDeniedException ex) {
        log(ex);
        return R.failed(401,
                config.getErrorMsgs().getOrDefault(ErrorConstant.PERMISSION_DENIED, ErrorConstant.PERMISSION_DENIED))
            .setError(ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @org.springframework.web.bind.annotation.ExceptionHandler(AuthenticationException.class)
    public R AuthenticationException(AuthenticationException ex) {
        log(ex);

        return R.failed(401, config.getErrorMsgs().getOrDefault(ErrorConstant.AUTHENTICATION_FAILED_OR_EXPIRED,
            ErrorConstant.AUTHENTICATION_FAILED_OR_EXPIRED)).setError(ex.getMessage());
    }
}
