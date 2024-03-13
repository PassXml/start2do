package org.start2do;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.PermissionException;
import org.start2do.dto.R;
import org.start2do.util.ValidateException;

@Slf4j
@ControllerAdvice
@RestControllerAdvice
@RequiredArgsConstructor
@Import(SpringCommonConfig.class)
public class ExceptionHandler {

    private final SpringCommonConfig config;

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(DataNotFoundException.class)
    public R DataNotFoundException(DataNotFoundException e) {
        log(e);
        return R.failed(5000, e.getMessage());
    }


    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(ValidateException.class)
    public R ValueException(ValidateException e) {
        log(e);
        return R.failed(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(PermissionException.class)
    public R PermissionException(PermissionException e) {
        log(e);
        return R.failed(5000, e.getMessage());
    }

    private void log(Exception e) {
        if (config.getErrorTrace() == null) {
            log.error(e.getMessage(), e);
        } else {
            log.error(e.getMessage(), e);
            for (StackTraceElement element : e.getStackTrace()) {
                if (element.getClassName().startsWith(config.getErrorTrace().getPackageName())) {
                    log.info("{},{},{}:{}", e.getMessage(), element.getClassName(), element.getMethodName(),
                        element.getLineNumber());
                    break;
                }
            }
        }
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(BusinessException.class)
    public R BusinessException(BusinessException e) {
        log(e);
        return R.failed(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public R Exception(Exception e) {
        log(e);
        return R.failed(500, e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public R Exception(MethodArgumentNotValidException e) {
        log(e);
        return R.failed(5000, e.getMessage());
    }
    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(WebExchangeBindException.class)
    public R handleWebExchangeBindException(WebExchangeBindException e) {
        log(e);
        String msg = e.getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining(", "));
                return R.failed(msg);
    }

}
