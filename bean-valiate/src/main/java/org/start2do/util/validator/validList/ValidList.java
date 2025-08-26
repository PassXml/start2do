package org.start2do.util.validator.validList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ValidListValidator.class) // 指定校验器
@Target({ElementType.PARAMETER, ElementType.FIELD}) // 可以用在方法参数和字段上
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidList {
    String message() default "列表中的对象校验失败";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
