package org.start2do.util;

import lombok.Getter;
import lombok.Setter;

public class ValidateException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    @Setter
    @Getter
    private Integer code;

    public ValidateException(String message) {
        super(message);
        code = 5000;
    }

}
