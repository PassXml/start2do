package org.start2do.util.validator.notPattern;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {InPatternValidation.class})
public @interface InPattern {

    String value() default "";

    boolean ignoreNull() default false;

    String message() default "${validatedValue} 不符合表达式{value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
