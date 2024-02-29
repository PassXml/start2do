package org.start2do.util.validator.ip;

import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class AbsIPValidation {

    protected Pattern pattern;


    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return pattern.matcher(value).matches();
    }
}
