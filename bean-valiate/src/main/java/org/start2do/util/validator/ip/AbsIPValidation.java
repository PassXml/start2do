package org.start2do.util.validator.ip;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidatorContext;

public class AbsIPValidation {

    protected Pattern pattern;


    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return pattern.matcher(value).matches();
    }
}
