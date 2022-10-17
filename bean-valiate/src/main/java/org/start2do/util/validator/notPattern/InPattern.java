package org.start2do.util.validator.notPattern;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {InPatternValidation.class})
public @interface InPattern {

    String value() default "";

    boolean checkNull() default true;

    String message() default "${validatedValue} 不符合表达式{value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
