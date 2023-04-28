package org.start2do.util.validator.notPattern;

import java.util.regex.Pattern;
import jakarta.validation.ConstraintValidator;

public class NotHasPatternValidation extends AbsNotHasPatternValidation implements
    ConstraintValidator<NotHasPattern, String> {


    @Override
    public void initialize(NotHasPattern constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.value());
        checkNull = constraintAnnotation.checkNull();
    }
}

