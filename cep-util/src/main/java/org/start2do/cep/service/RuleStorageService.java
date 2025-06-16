package org.start2do.cep.service;

import java.util.Map;
import org.start2do.cep.dto.CEPRule;

public interface RuleStorageService {
    void saveRule(CEPRule rule);
    CEPRule loadRule(String ruleId);
    Map<String, CEPRule> loadAllRules();
    void deleteRule(String ruleId);
}
