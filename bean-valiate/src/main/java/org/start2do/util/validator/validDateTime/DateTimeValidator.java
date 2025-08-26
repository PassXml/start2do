package org.start2do.util.validator.validDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateTimeValidator implements ConstraintValidator<ValidDateTime, String> {

    private static final ConcurrentHashMap<String, DateTimeFormatter> CACHE = new ConcurrentHashMap<>(5);
    private DateTimeFormatter formatter;

    @Override
    public void initialize(ValidDateTime ann) {
        this.formatter = CACHE.computeIfAbsent(ann.pattern(),
            p -> DateTimeFormatter.ofPattern(p).withResolverStyle(ResolverStyle.STRICT));
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            formatter.parse(value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
