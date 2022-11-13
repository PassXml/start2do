package org.start2do.util;

import java.util.Set;
import java.util.StringJoiner;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public final class BeanValidatorUtil {

    protected static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    protected static Validator validator = factory.getValidator();
    public static Boolean echoPath = false;

    public static void validate(Object obj, Class<?>... groups) {
        validate(echoPath, obj, groups);
    }

    public static void validateEchoPath(Object obj, Class<?>... groups) {
        validate(true, obj, groups);
    }

    public static void validate(Boolean echoPath, Object obj, Class<?>... groups) {
        String s = _validate(echoPath, obj, groups);
        if (s != null && s.length() > 0) {
            throw new ValidateException(s);
        }
    }


    private static String _validate(Boolean echoPath, Object obj, Class... clazz) {
        Set<ConstraintViolation<Object>> set = validator.validate(obj, clazz);
        StringJoiner result = new StringJoiner(",");
        for (ConstraintViolation<Object> vResult : set) {
            if (echoPath) {
                result.add("字段:" + vResult.getPropertyPath() + ":" + vResult.getMessage());
            } else {
                result.add(vResult.getMessage());
            }
        }
        return result.toString();
    }

}
