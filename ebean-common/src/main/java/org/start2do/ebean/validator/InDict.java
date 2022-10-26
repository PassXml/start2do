package org.start2do.ebean.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {InArrayDictItemValidator.class})
public @interface InDict {

    Class clazz();


    boolean checkNull() default true;

    String message() default "${validatedValue} 不在{value}的取值范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
