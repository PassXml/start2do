package org.start2do.cep.util;

import lombok.experimental.UtilityClass;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@UtilityClass
public class ELUtil {
    private final ExpressionParser spelParser = new SpelExpressionParser();

}
