package org.start2do.util.validator.ip;

import jakarta.validation.ConstraintValidator;
import java.util.regex.Pattern;

public class IPV4Validation extends AbsIPValidation implements ConstraintValidator<ValidIPv4, String> {

    @Override
    public void initialize(ValidIPv4 value) {
        ConstraintValidator.super.initialize(value);
        pattern = Pattern.compile(value.value());
    }
}
