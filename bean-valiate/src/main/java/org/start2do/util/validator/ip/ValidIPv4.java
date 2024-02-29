package org.start2do.util.validator.ip;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IPV4Validation.class})

public @interface ValidIPv4 {

    String value() default "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)";

    boolean checkNull() default true;

    String message() default "${validatedValue} 非法IP";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
