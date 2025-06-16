package org.start2do.cep.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.start2do.cep.dto.CEPRule;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleManagementService {

    private final RuleStorageService ruleStorageService;
    private final Map<String, CEPRule> activeRules = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeRules() {
        try {
            log.info("从存储中初始化规则...");
            List<CEPRule> rules = ruleStorageService.loadRules();
            for (CEPRule rule : rules) {
                activeRules.put(rule.getRuleId(), rule);
            }
            log.info("成功加载 {} 条规则到内存中。", rules.size());
        } catch (IOException e) {
            log.error("初始化规则失败", e);
            // 在生产环境中，您可能希望抛出异常以阻止应用启动
        }
    }

    public void createRule(CEPRule rule) throws IOException {
        if (activeRules.containsKey(rule.getRuleId())) {
            throw new IllegalArgumentException("规则ID已存在: " + rule.getRuleId());
        }
        ruleStorageService.saveRule(rule);
        activeRules.put(rule.getRuleId(), rule);
        log.info("已创建并持久化新规则: {}", rule.getRuleId());
    }

    public void updateRule(CEPRule rule) throws IOException {
        if (!activeRules.containsKey(rule.getRuleId())) {
            throw new IllegalArgumentException("规则ID不存在: " + rule.getRuleId());
        }
        ruleStorageService.saveRule(rule);
        activeRules.put(rule.getRuleId(), rule);
        log.info("已更新并持久化规则: {}", rule.getRuleId());
    }

    public void deleteRule(String ruleId) throws IOException {
        if (activeRules.containsKey(ruleId)) {
            ruleStorageService.deleteRule(ruleId);
            activeRules.remove(ruleId);
            log.info("已删除规则: {}", ruleId);
        }
    }

    public void enableRule(String ruleId) throws IOException {
        CEPRule rule = activeRules.get(ruleId);
        if (rule != null) {
            rule.setEnabled(true);
            updateRule(rule);
        }
    }

    public void disableRule(String ruleId) throws IOException {
        CEPRule rule = activeRules.get(ruleId);
        if (rule != null) {
            rule.setEnabled(false);
            updateRule(rule);
        }
    }

    public CEPRule getRule(String ruleId) {
        return activeRules.get(ruleId);
    }

    public List<CEPRule> getAllActiveRules() {
        return activeRules.values().stream()
            .filter(CEPRule::isEnabled)
            .collect(Collectors.toList());
    }
}
