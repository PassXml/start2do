package org.start2do.util;

import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@UtilityClass
public class ELUtil {

    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    public String parseSpel(ProceedingJoinPoint point, String spel) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

        // StandardEvaluationContext 不是线程安全的，需要每次创建新实例
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 获取参数名
        String[] parameterNames = discoverer.getParameterNames(method);

        // 设置方法参数
        if (parameterNames != null) {
            Object[] args = point.getArgs();
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        // 设置内置变量
        context.setVariable("method", method.getName());
        context.setVariable("class", method.getDeclaringClass().getSimpleName());
        context.setVariable("target", point.getTarget());

        Expression expression = parser.parseExpression(spel);
        Object value = expression.getValue(context);
        return value != null ? value.toString() : "";
    }
}
