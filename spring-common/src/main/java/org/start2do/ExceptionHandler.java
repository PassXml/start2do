package org.start2do;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.start2do.dto.BusinessException;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.PermissionException;
import org.start2do.dto.R;
import org.start2do.dto.RateLimiterException;
import org.start2do.util.ValidateException;
import reactor.core.publisher.Mono;

@Slf4j
@ControllerAdvice
@ConditionalOnProperty(
    prefix = "start2do",
    name = "enable-exception",
    matchIfMissing = true,
    havingValue = "true")
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
    String uri = getRequestUri();
    if (config.getErrorTrace() == null) {
      log.error("请求URI: {}, 错误信息: {}", uri, e.getMessage(), e);
    } else {
      log.error("请求URI: {}, 错误信息: {}", uri, e.getMessage(), e);
      for (StackTraceElement element : e.getStackTrace()) {
        if (element.getClassName().startsWith(config.getErrorTrace().getPackageName())) {
          log.info(
              "请求URI: {}, 错误信息: {}, 类: {}, 方法: {}, 行号: {}",
              uri,
              e.getMessage(),
              element.getClassName(),
              element.getMethodName(),
              element.getLineNumber());
          break;
        }
      }
    }
  }

  // 获取请求URI，支持Servlet和WebFlux
  private String getRequestUri() {
    try {
      // 尝试获取Servlet环境下的URI
      RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
      if (attributes instanceof ServletRequestAttributes) {
        ServletRequestAttributes servletAttributes = (ServletRequestAttributes) attributes;
        return servletAttributes.getRequest().getRequestURI();
      }
    } catch (Exception e) {
      log.warn("无法从Servlet环境获取URI: {}", e.getMessage());
    }
    // 如果不是Servlet环境，尝试WebFlux环境
    try {
      return Mono.deferContextual(
              contextView -> {
                ServerWebExchange exchange = contextView.get(ServerWebExchange.class);
                return Mono.just(exchange.getRequest().getURI().toString());
              })
          .blockOptional()
          .orElse("未知URI (WebFlux环境未获取到)");
    } catch (Exception e) {
      log.warn("无法从WebFlux环境获取URI: {}", e.getMessage());
    }
    return "未知URI";
  }

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
    return R.failed(message).setError(message);
  }

  @ResponseBody
  @org.springframework.web.bind.annotation.ExceptionHandler(value = RateLimiterException.class)
  public R RateLimiterException(RateLimiterException e) {
    return R.failed(e.getMessage()).setError("速率限制");
  }

  public ExceptionHandler(SpringCommonConfig config) {
    log.info("初始化ExceptionHandler");
    this.config = config;
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @org.springframework.web.bind.annotation.ExceptionHandler(NoHandlerFoundException.class)
  public R handleNotFound(NoHandlerFoundException ex) {
    log(ex);
    return R.failed("资源不存在").setError(ex.getMessage());
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
  public R accessDeniedException(AccessDeniedException ex) {
    log(ex);
    return R.failed(401, "权限不足").setError(ex.getMessage());
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @org.springframework.web.bind.annotation.ExceptionHandler(AuthenticationException.class)
  public R AuthenticationException(AuthenticationException ex) {
    log(ex);
    return R.failed(401, "认证失败或者凭证过期").setError(ex.getMessage());
  }
}
