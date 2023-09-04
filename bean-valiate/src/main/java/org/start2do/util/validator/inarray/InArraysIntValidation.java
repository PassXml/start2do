package org.start2do.util.validator.inarray;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class InArraysIntValidation implements ConstraintValidator<InArray, Integer> {

    private Set<Integer> set = new HashSet<>();
    private Boolean ignoreNull;


    @Override
    public void initialize(InArray constraintAnnotation) {
        for (String s : constraintAnnotation.value()) {
            set.add(Integer.parseInt(s));
        }
        this.ignoreNull = constraintAnnotation.ignoreNull();
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        if (integer == null) {
            return ignoreNull;
        }
        if (set.contains(integer)) {
            return true;
        }
        return false;
    }
}
