package org.start2do.util.validator.validDateTime;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 校验字符串是否为合法的 yyyy-MM-dd HH:mm:ss 日期时间
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = DateTimeValidator.class)
public @interface ValidDateTime {

    String pattern() default "yyyy-MM-dd HH:mm:ss";

    String message() default "日期时间必须符合 '${pattern}'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
