package org.start2do.util.validator.notPattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;


public class InPatternValidation implements ConstraintValidator<InPattern, String> {

    protected Pattern pattern;
    protected Boolean ignoreNull;

    @Override
    public void initialize(InPattern constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.value());
        ignoreNull = constraintAnnotation.ignoreNull();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return ignoreNull;
        }
        boolean matches = pattern.matcher(s).matches();
        return matches;
    }
}

