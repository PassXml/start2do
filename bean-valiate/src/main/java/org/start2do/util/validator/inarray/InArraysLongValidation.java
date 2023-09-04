package org.start2do.util.validator.inarray;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class InArraysLongValidation implements ConstraintValidator<InArray, Long> {

    private Set<Long> set = new HashSet<>();
    private boolean ignoreNull;

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
        return false;
    }
}
