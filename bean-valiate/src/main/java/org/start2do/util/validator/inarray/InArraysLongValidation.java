package org.start2do.util.validator.inarray;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class InArraysLongValidation implements ConstraintValidator<InArray, Long> {

    private Set<Long> set = new HashSet<>();
    private Boolean checkNull;

    @Override
    public void initialize(InArray constraintAnnotation) {
        for (String s : constraintAnnotation.value()) {
            set.add(Long.parseLong(s));
        }
        this.checkNull = constraintAnnotation.checkNull();
    }

    @Override
    public boolean isValid(Long aLong, ConstraintValidatorContext constraintValidatorContext) {
        if (aLong == null) {
            return !checkNull;
        }
        if (set.contains(aLong)) {
            return true;
        }
        return false;
    }
}
