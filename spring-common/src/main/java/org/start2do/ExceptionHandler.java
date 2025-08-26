package org.start2do;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.PermissionException;
import org.start2do.dto.R;
import org.start2do.dto.RateLimiterException;
import org.start2do.util.ValidateException;

@Slf4j
@ControllerAdvice
@ConditionalOnProperty(
    prefix = "start2do",
    name = "enable-exception",
    matchIfMissing = true,
    havingValue = "true")
public class ExceptionHandler extends BaseExceptionHandler {


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

    // 获取请求URI，支持Servlet和WebFlux

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(BusinessException.class)
    public R BusinessException(BusinessException e) {
        log(e);
        return R.failed(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(MaxUploadSizeExceededException.class)
    public R BusinessException(MaxUploadSizeExceededException e) {
        log(e);
        return R.failed(5000, "超出文件允许大小");
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(BindException.class)
    public R BusinessException(BindException e) {
        log(e);
        StringJoiner joiner = new StringJoiner(";");
        for (FieldError error : e.getFieldErrors()) {
            joiner.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return R.failed(5000, "参数绑定错误").setError(joiner.toString());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public R Exception(Exception e) {
        log(e);
        return R.failed(500, "系统错误").setError(e.getMessage());
    }

    // 在 ExceptionHandler 类中添加新方法或修改现有方法

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(
        value = MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        String message =
            allErrors.stream()
                .map(
                    objectError -> {
                        StringJoiner joiner = new StringJoiner(",");
                        if (objectError.getArguments() != null) {
                            for (Object argument : objectError.getArguments()) {
                                if (argument instanceof DefaultMessageSourceResolvable error) {
                                    joiner.add(error.getDefaultMessage());
                                }
                            }
                        }
                        return joiner + objectError.getDefaultMessage();
                    })
                .collect(Collectors.joining(";"));
        logRequestBody();
        return R.failed(message).setError(message);
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(value = RateLimiterException.class)
    public R RateLimiterException(RateLimiterException e) {
        return R.failed(e.getMessage()).setError("速率限制");
    }

    public ExceptionHandler(SpringCommonConfig config) {
        super(config);
        log.info("初始化ExceptionHandler");
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(NoHandlerFoundException.class)
    public R handleNotFound(NoHandlerFoundException ex) {
        log(ex);
        return R.failed("资源不存在").setError(ex.getMessage());
    }



}
