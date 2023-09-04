package org.start2do.util.validator.notPattern;

import java.util.regex.Pattern;
import jakarta.validation.ConstraintValidator;

public class NotHasEmojiValidation extends AbsNotHasPatternValidation implements
    ConstraintValidator<NotHasEmoji, String> {


    @Override
    public void initialize(NotHasEmoji constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.value());
        ignoreNull = constraintAnnotation.ignoreNull();
    }
}

