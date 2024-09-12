package org.start2do.util.validator.list;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import org.start2do.util.BeanValidatorUtil;

public class ValidListItemValidation implements ConstraintValidator<ValidListItem, Collection> {

    private ValidListItem validListItem;

    @Override
    public void initialize(ValidListItem constraintAnnotation) {
        this.validListItem = constraintAnnotation;

    }

    @Override
    public boolean isValid(Collection value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int i = 0;
        for (Object o : value) {
            if (o == null) {
                i++;
                continue;
            }
            try {
                BeanValidatorUtil.validate(true, o);
            } catch (Exception exception) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("%s,第%d元素,%s", context.getDefaultConstraintMessageTemplate(), i,
                        exception.getMessage())
                ).addConstraintViolation();

                return false;
            }
            i++;
        }
        return true;
    }
}
