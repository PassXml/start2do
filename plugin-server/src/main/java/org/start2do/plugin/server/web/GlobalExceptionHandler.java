package org.start2do.plugin.server.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.start2do.plugin.api.dto.ApiResponse;

/**
 * plugin-server 模块统一异常处理，将异常转换为统一的 ApiResponse 返回结构
 */
@Slf4j
@RestControllerAdvice(basePackages = "org.start2do.plugin.server.web")
public class GlobalExceptionHandler {

    /**
     * 参数校验异常
     */
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        log.warn("请求参数错误: {}", ex.getMessage(), ex);
        ApiResponse<Void> body = ApiResponse.error(40001, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleServerError(Exception ex) {
        log.error("服务内部错误: {}", ex.getMessage(), ex);
        ApiResponse<Void> body = ApiResponse.error(50001, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

