package org.start2do.util.validator.notPattern;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidatorContext;

public abstract class AbsNotHasPatternValidation {

    protected Pattern pattern;
    protected Boolean checkNull;

    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return !checkNull;
        }
        boolean matches = pattern.matcher(s).find();
        return !matches;
    }
}
