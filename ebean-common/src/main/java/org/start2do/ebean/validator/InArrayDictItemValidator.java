package org.start2do.ebean.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.start2do.ebean.dict.IDictItem;

public class InArrayDictItemValidator implements ConstraintValidator<InDict, IDictItem> {

    @Override
    public boolean isValid(IDictItem value, ConstraintValidatorContext context) {
        return false;
    }
}
