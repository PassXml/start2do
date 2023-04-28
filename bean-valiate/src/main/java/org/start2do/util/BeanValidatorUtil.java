package org.start2do.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.StringJoiner;

public final class BeanValidatorUtil {

    protected static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    protected static Validator validator = factory.getValidator();
    private static Boolean echoPath = false;

    public static void setEchoPath() {
        BeanValidatorUtil.echoPath = true;
    }

    public static void setHidePath() {
        BeanValidatorUtil.echoPath = false;
    }

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
