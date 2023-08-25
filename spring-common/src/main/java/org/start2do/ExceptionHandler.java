package org.start2do;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.PermissionException;
import org.start2do.dto.R;
import org.start2do.util.ValidateException;

@Slf4j
@ControllerAdvice
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
            for (StackTraceElement element : e.getStackTrace()) {
                if (element.getClassName().startsWith(config.getErrorTrace().getPackageName())) {
                    log.error("{},{},{}:{}", e.getMessage(), element.getClassName(), element.getMethodName(),
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
    @org.springframework.web.bind.annotation.ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        String message = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(";"));
        return R.failed(message);
    }
}
