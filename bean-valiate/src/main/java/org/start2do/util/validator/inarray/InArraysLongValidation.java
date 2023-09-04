package org.start2do.util.validator.inarray;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InArraysLongValidation implements ConstraintValidator<InArray, Long> {

    private Set<Long> set = new HashSet<>();
    private Boolean ignoreNull;

    @Override
    public void initialize(InArray constraintAnnotation) {
        for (String s : constraintAnnotation.value()) {
            set.add(Long.parseLong(s));
        }
        this.ignoreNull = constraintAnnotation.ignoreNull();
    }

    @Override
    public boolean isValid(Long aLong, ConstraintValidatorContext constraintValidatorContext) {
        if (aLong == null) {
            return ignoreNull;
        }
        if (set.contains(aLong)) {
            return true;
        }
        set.clear();
        set = null;
        return false;
    }
}
