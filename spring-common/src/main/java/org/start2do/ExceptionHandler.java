package org.start2do;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.PermissionException;
import org.start2do.dto.R;
import org.start2do.util.ValidateException;

@Slf4j
@ControllerAdvice
public class ExceptionHandler {


    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(DataNotFoundException.class)
    public R DataNotFoundException(DataNotFoundException e) {
        log.error(e.getMessage(), e);
        return R.failed(5000, e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(ValidateException.class)
    public R ValueException(ValidateException e) {
        log.error(e.getMessage(), e);
        return R.failed(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(PermissionException.class)
    public R PermissionException(PermissionException e) {
        log.error(e.getMessage(), e);
        return R.failed(5000, e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(BusinessException.class)
    public R BusinessException(BusinessException e) {
        log.error(e.getMessage(), e);
        return R.failed(e.getCode(), e.getMessage());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public R Exception(Exception e) {
        log.error(e.getMessage(), e);
        return R.failed(500, e.getMessage());
    }
}
