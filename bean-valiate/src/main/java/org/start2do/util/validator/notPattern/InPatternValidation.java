package org.start2do.util.validator.notPattern;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InPatternValidation implements ConstraintValidator<InPattern, String> {

    protected Pattern pattern;
    protected Boolean checkNull;

    @Override
    public void initialize(InPattern constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.value());
        checkNull = constraintAnnotation.checkNull();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return !checkNull;
        }
        boolean matches = pattern.matcher(s).matches();
        return matches;
    }
}

