package org.start2do.dto;

/**
 * @Author Lijie
 * @date 2021/12/15:17:29
 */
@lombok.Getter
@lombok.Setter
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;

    }

    public BusinessException(String message) {
        super(message);
        this.code = 5000;
    }

    public static void nowThrow(String msg) {
        throw new BusinessException(msg);
    }
}
