package org.start2do.util.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import org.start2do.ebean.dict.IDictItem;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InEnumValue.Validator.class)
public @interface InEnumValue {

    String message() default "${validatedValue} 无效的值";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends IDictItem> value();


    class Validator implements ConstraintValidator<InEnumValue, String> {

        private Class<? extends IDictItem> enumClass;

        @Override
        public void initialize(InEnumValue enumValue) {
            enumClass = enumValue.value();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            if (value == null) {
                return Boolean.TRUE;
            }
            if (enumClass == null) {
                return Boolean.TRUE;
            }
            return IDictItem.find(enumClass, value) != null;
        }
    }

}
