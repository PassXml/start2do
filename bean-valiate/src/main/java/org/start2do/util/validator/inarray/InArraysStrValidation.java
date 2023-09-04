package org.start2do.util.validator.inarray;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InArraysStrValidation implements ConstraintValidator<InArray, String> {

    private Set<String> set = new HashSet<>();
    private Boolean ignoreNull = false;

    @Override
    public void initialize(InArray constraintAnnotation) {
        for (String s : constraintAnnotation.value()) {
            set.add(s);
        }
        this.ignoreNull = constraintAnnotation.ignoreNull();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return ignoreNull;
        }
        if (set.contains(s)) {
            return true;
        }
        set.clear();
        set = null;
        return false;
    }
}
