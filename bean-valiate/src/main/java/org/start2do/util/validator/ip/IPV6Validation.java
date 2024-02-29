package org.start2do.util.validator.ip;

import jakarta.validation.ConstraintValidator;
import java.util.regex.Pattern;

public class IPV6Validation extends AbsIPValidation implements ConstraintValidator<ValidIPv6, String> {

    @Override
    public void initialize(ValidIPv6 value) {
        ConstraintValidator.super.initialize(value);
        pattern = Pattern.compile(value.value());
    }
}
