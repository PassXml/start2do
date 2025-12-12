package org.start2do.plugin.api.dto;

import lombok.Data;

/**
 * 统一 API 返回结构
 *
 * @param <T> 业务数据类型
 */
@Data
public class ApiResponse<T> {

    /**
     * 业务状态码：0 表示成功，非 0 表示业务错误
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    /**
     * 失败返回
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

