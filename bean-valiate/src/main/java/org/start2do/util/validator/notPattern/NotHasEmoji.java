package org.start2do.util.validator.notPattern;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NotHasEmojiValidation.class})
public @interface NotHasEmoji {

    String value() default "[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]";

    boolean checkNull() default true;

    String message() default "${validatedValue} 不能包含Emoji表情";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
