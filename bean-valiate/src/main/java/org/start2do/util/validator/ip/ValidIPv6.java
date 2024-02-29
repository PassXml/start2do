package org.start2do.util.validator.ip;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IPV6Validation.class})
public @interface ValidIPv6 {

    String value() default "^([\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}|:((:[\\da−fA−F]1,4)1,6|:)";

    boolean checkNull() default true;

    String message() default "${validatedValue} 非法IP";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
