package org.start2do.cep.service;

import org.start2do.cep.dto.CEPRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class DynamicRuleManagementService {
    private static final Logger log = LoggerFactory.getLogger(DynamicRuleManagementService.class);
    private static final Map<String, CEPRule> ruleMap = new ConcurrentHashMap<>();
    private static final List<RuleChangeListener> listeners = new CopyOnWriteArrayList<>();

    public interface RuleChangeListener {
        void onRuleAdded(CEPRule rule);
        void onRuleUpdated(CEPRule rule);
        void onRuleDeleted(String ruleId);
    }

    public static void addRule(CEPRule rule) {
        if (rule == null || rule.getRuleId() == null) {
            throw new IllegalArgumentException("Rule and rule ID cannot be null");
        }
        
        CEPRule existingRule = ruleMap.get(rule.getRuleId());
        ruleMap.put(rule.getRuleId(), rule);
        
        if (existingRule == null) {
            log.info("Added new rule: {}", rule.getRuleId());
            notifyRuleAdded(rule);
        } else {
            log.info("Updated rule: {}", rule.getRuleId());
            notifyRuleUpdated(rule);
        }
    }

    public static void addRules(List<CEPRule> rules) {
        if (rules == null) {
            return;
        }
        
        for (CEPRule rule : rules) {
            addRule(rule);
        }
    }

    public static CEPRule getRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    public static List<CEPRule> getAllRules() {
        return new ArrayList<>(ruleMap.values());
    }

    public static List<CEPRule> getActiveRules() {
        List<CEPRule> activeRules = new ArrayList<>();
        for (CEPRule rule : ruleMap.values()) {
            if (rule.isEnabled()) {
                activeRules.add(rule);
            }
        }
        return activeRules;
    }

    public static CEPRule updateRule(CEPRule rule) {
        if (rule == null || rule.getRuleId() == null) {
            throw new IllegalArgumentException("Rule and rule ID cannot be null");
        }
        
        if (!ruleMap.containsKey(rule.getRuleId())) {
            throw new IllegalArgumentException("Rule not found: " + rule.getRuleId());
        }
        
        ruleMap.put(rule.getRuleId(), rule);
        log.info("Updated rule: {}", rule.getRuleId());
        notifyRuleUpdated(rule);
        
        return rule;
    }

    public static CEPRule deleteRule(String ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("Rule ID cannot be null");
        }
        
        CEPRule removedRule = ruleMap.remove(ruleId);
        if (removedRule != null) {
            log.info("Deleted rule: {}", ruleId);
            notifyRuleDeleted(ruleId);
        }
        
        return removedRule;
    }

    public static boolean setRuleEnabled(String ruleId, boolean enabled) {
        CEPRule rule = ruleMap.get(ruleId);
        if (rule != null) {
            rule.setEnabled(enabled);
            log.info("Set rule {} enabled: {}", ruleId, enabled);
            notifyRuleUpdated(rule);
            return true;
        }
        return false;
    }

    public static boolean hasRule(String ruleId) {
        return ruleMap.containsKey(ruleId);
    }

    public static int getRuleCount() {
        return ruleMap.size();
    }

    public static int getActiveRuleCount() {
        return (int) ruleMap.values().stream().filter(CEPRule::isEnabled).count();
    }

    public static void clearAllRules() {
        ruleMap.clear();
        log.info("Cleared all rules");
    }

    public static void addRuleChangeListener(RuleChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public static void removeRuleChangeListener(RuleChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyRuleAdded(CEPRule rule) {
        for (RuleChangeListener listener : listeners) {
            try {
                listener.onRuleAdded(rule);
            } catch (Exception e) {
                log.error("Error notifying rule added listener for rule: " + rule.getRuleId(), e);
            }
        }
    }

    private static void notifyRuleUpdated(CEPRule rule) {
        for (RuleChangeListener listener : listeners) {
            try {
                listener.onRuleUpdated(rule);
            } catch (Exception e) {
                log.error("Error notifying rule updated listener for rule: " + rule.getRuleId(), e);
            }
        }
    }

    private static void notifyRuleDeleted(String ruleId) {
        for (RuleChangeListener listener : listeners) {
            try {
                listener.onRuleDeleted(ruleId);
            } catch (Exception e) {
                log.error("Error notifying rule deleted listener for rule: " + ruleId, e);
            }
        }
    }
}