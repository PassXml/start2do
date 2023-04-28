package org.start2do.util.validator.inarray;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class InArraysStrValidation implements ConstraintValidator<InArray, String> {

    private Set<String> set = new HashSet<>();
    private Boolean checkNull;

    @Override
    public void initialize(InArray constraintAnnotation) {
        for (String s : constraintAnnotation.value()) {
            set.add(s);
        }
        this.checkNull = constraintAnnotation.checkNull();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return !checkNull;
        }
        if (set.contains(s)) {
            return true;
        }
        return false;
    }
}
