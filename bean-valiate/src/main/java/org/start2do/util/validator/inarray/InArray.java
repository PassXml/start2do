package org.start2do.util.validator.inarray;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {InArraysStrValidation.class, InArraysIntValidation.class, InArraysLongValidation.class})
public @interface InArray {

    String[] value() default {};

    boolean checkNull() default true;

    String message() default "${validatedValue} 不在{value}的取值范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
