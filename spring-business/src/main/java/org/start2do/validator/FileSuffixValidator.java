package org.start2do.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FileTypeValidatorValidation.class})
public @interface FileSuffixValidator {

    String[] value() default {};


    String message() default "文件类型不允许,只允许{value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
