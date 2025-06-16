package org.start2do.cep.service;

import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *  规则管理服务
 */
@Service
public class RuleManagementService {

    private static final Logger logger = LoggerFactory.getLogger(RuleManagementService.class);
    private final org.start2do.cep.pettern.DynamicPatternManager patternManager;
    private final RuleStorageService ruleStorageService;

    public RuleManagementService(RuleStorageService ruleStorageService) {
        this.patternManager = org.start2do.cep.pettern.DynamicPatternManager.getInstance();
        this.ruleStorageService = ruleStorageService;
    }

    @PostConstruct
    public void initializeRules() {
        logger.info("从存储中初始化规则...");
        Map<String, org.start2do.cep.dto.CEPRule> storedRules = ruleStorageService.loadAllRules();
        if (storedRules == null || storedRules.isEmpty()) {
            logger.info("存储中未找到规则.");
            return;
        }

        for (org.start2do.cep.dto.CEPRule rule : storedRules.values()) {
            try {
                patternManager.addOrUpdateRule(rule);
                logger.info("已加载规则: {} (启用: {})", rule.getRuleId(), rule.isEnabled());
            } catch (Exception e) {
                logger.error("加载规则失败: {}", rule.getRuleId(), e);
            }
        }
        logger.info("规则初始化完成. 总共加载: {}", storedRules.size());
    }

    /**
     * 创建规则
     */
    public void createRule(org.start2do.cep.dto.CEPRule rule) {
        try {
            validateRule(rule);
            patternManager.addOrUpdateRule(rule);
            ruleStorageService.saveRule(rule);
            logger.info("规则创建成功: {}", rule.getRuleId());
        } catch (Exception e) {
            logger.error("创建规则失败: {}", rule.getRuleId(), e);
            throw new RuntimeException("创建规则失败", e);
        }
    }

    /**
     * 更新规则
     */
    public void updateRule(org.start2do.cep.dto.CEPRule rule) {
        try {
            validateRule(rule);
            patternManager.addOrUpdateRule(rule);
            ruleStorageService.saveRule(rule);
            logger.info("规则更新成功: {}", rule.getRuleId());
        } catch (Exception e) {
            logger.error("更新规则失败: {}", rule.getRuleId(), e);
            throw new RuntimeException("更新规则失败", e);
        }
    }

    /**
     * 删除规则
     */
    public void deleteRule(String ruleId) {
        try {
            patternManager.removeRule(ruleId);
            ruleStorageService.deleteRule(ruleId);
            logger.info("规则删除成功: {}", ruleId);
        } catch (Exception e) {
            logger.error("删除规则失败: {}", ruleId, e);
            throw new RuntimeException("删除规则失败", e);
        }
    }

    /**
     * 获取规则
     */
    public org.start2do.cep.dto.CEPRule getRule(String ruleId) {
        return patternManager.getRule(ruleId);
    }

    /**
     * 获取所有规则
     */
    public Map<String, org.start2do.cep.dto.CEPRule> getAllRules() {
        return patternManager.getActiveRules();
    }

    /**
     * 启用规则
     */
    public void enableRule(String ruleId) {
        org.start2do.cep.dto.CEPRule rule = patternManager.getRule(ruleId);
        if (rule != null) {
            rule.setEnabled(true);
            patternManager.addOrUpdateRule(rule);
            ruleStorageService.saveRule(rule);
            logger.info("规则已启用: {}", ruleId);
        }
    }

    /**
     * 禁用规则
     */
    public void disableRule(String ruleId) {
        org.start2do.cep.dto.CEPRule rule = patternManager.getRule(ruleId);
        if (rule != null) {
            rule.setEnabled(false);
            patternManager.addOrUpdateRule(rule);
            ruleStorageService.saveRule(rule);
            logger.info("规则已禁用: {}", ruleId);
        }
    }

    /**
     * 验证规则
     */
    private void validateRule(org.start2do.cep.dto.CEPRule rule) {
        if (rule.getRuleId() == null || rule.getRuleId().trim().isEmpty()) {
            throw new IllegalArgumentException("规则ID不能为空");
        }
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        if (rule.getPattern() == null || rule.getPattern().isEmpty()) {
            throw new IllegalArgumentException("规则模式不能为空");
        }
        // 验证模式步骤
        for (org.start2do.cep.dto.CEPRule.PatternStep step : rule.getPattern()) {
            if (step.getStepName() == null || step.getStepName().trim().isEmpty()) {
                throw new IllegalArgumentException("模式步骤名称不能为空");
            }
            if (step.getEventType() == null || step.getEventType().trim().isEmpty()) {
                throw new IllegalArgumentException("事件类型不能为空");
            }
        }
    }
}
