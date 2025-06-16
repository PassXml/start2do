package org.start2do.cep.action.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.start2do.cep.action.IAction;
import org.start2do.cep.dto.PatternMatchResult;

@Slf4j
@Component
public class LogAction implements IAction {
    private static final long serialVersionUID = 1L;

    @Override
    public String type() {
        return "log";
    }

    @Override
    public void execute(PatternMatchResult result) {
        log.info("规则匹配成功 [Action: Log]: ruleId={}, ruleName={}, matchTime={}, matchedEvents={}",
            result.getRuleId(),
            result.getRuleName(),
            result.getMatchTimestamp(),
            result.getMatchedEvents().toString()
        );
    }
}
