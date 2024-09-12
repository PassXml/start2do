package org.start2do.util.validator.list;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lijie
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidListItemValidation.class})
public @interface ValidListItem {

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
