package org.start2do.bpm.service;

import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.strategy.ExpressionStrategy;
import org.dromara.warm.flow.core.utils.ExpressionUtil;
import org.springframework.stereotype.Component;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.util.ScriptRunner;

@Slf4j
@Component
public class ScriptExpressionStrategy implements ExpressionStrategy {

    @PostConstruct
    public void init() {
        log.info("初始化脚本表达式");
        ExpressionUtil.setExpression(this);
    }

    @Override
    public String getType() {
        return "script";
    }

    @Override
    public void setExpression(ExpressionStrategy expressionStrategy) {

    }

    @Override
    public Object eval(String s, Map map) {
        log.info("执行脚本表达式,{}", s);
        ScriptRunnerResult result = ScriptRunner.evalById(s, "vars", map);
        log.info("执行脚本表达式结果:{}", result);
        return result.getResult();
    }
}
