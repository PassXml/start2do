package org.start2do.cep.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@UtilityClass
@Slf4j
public class ELUtil {
    private final ExpressionParser spelParser = new SpelExpressionParser();

    /**
     * 评估SpEL表达式，并返回一个布尔结果。
     *
     * @param expressionString SpEL表达式字符串. 例如: "#{#value > 10}"
     * @param actualValue      要在表达式中用作#value变量的值
     * @return 表达式评估的布尔结果. 如果评估成功且结果为true，则返回true；否则返回false。
     */
    public boolean evaluate(String expressionString, Object actualValue) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("value", actualValue);
            Expression expression = spelParser.parseExpression(expressionString);
            Boolean result = expression.getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.error("SpEL expression evaluation failed for expression: {}", expressionString, e);
            return false;
        }
    }
}
