package org.start2do.util.validator.notPattern;

import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;


public abstract class AbsNotHasPatternValidation {

    protected Pattern pattern;
    protected Boolean ignoreNull;

    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return ignoreNull;
        }
        boolean matches = pattern.matcher(s).find();
        return !matches;
    }
}
