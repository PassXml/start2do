package org.start2do.util.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.start2do.ebean.dict.IDictItem;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InEnumValue.Validator.class)
public @interface InEnumValue {

    String message() default "${validatedValue} 无效的值";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends IDictItem> value();

    boolean ignoreNull() default false;


    class Validator implements ConstraintValidator<InEnumValue, String> {

        private Class<? extends IDictItem> enumClass;
        private Boolean ignoreNull;

        @Override
        public void initialize(InEnumValue enumValue) {
            enumClass = enumValue.value();
            this.ignoreNull = enumValue.ignoreNull();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            if (value == null) {
                return ignoreNull;
            }
            if (enumClass == null) {
                return Boolean.TRUE;
            }
            return IDictItem.find(enumClass, value) != null;
        }
    }

}
