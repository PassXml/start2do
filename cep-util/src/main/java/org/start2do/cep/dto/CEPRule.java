package org.start2do.cep.dto;

import java.util.List;
import java.util.Map;

public class CEPRule {
    private String ruleId;
    private String ruleName;
    private String description;
    private boolean enabled;
    private int timeWindowSeconds;
    private List<PatternStep> pattern;
    private String action;
    private Map<String, Object> actionBody;

    public static class PatternStep {
        private String stepName;
        private String eventType;
        private Map<String, Object> filters;
        private PatternQuantifier quantifier;

        public enum PatternQuantifier {
            ONE, ONE_OR_MORE, ZERO_OR_MORE, ZERO_OR_ONE, TIMES
        }

        public String getStepName() {
            return stepName;
        }

        public PatternStep setStepName(String stepName) {
            this.stepName = stepName;
            return this;
        }

        public String getEventType() {
            return eventType;
        }

        public PatternStep setEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Map<String, Object> getFilters() {
            return filters;
        }

        public PatternStep setFilters(Map<String, Object> filters) {
            this.filters = filters;
            return this;
        }

        public PatternQuantifier getQuantifier() {
            return quantifier;
        }

        public PatternStep setQuantifier(PatternQuantifier quantifier) {
            this.quantifier = quantifier;
            return this;
        }
    }

    public String getRuleId() {
        return ruleId;
    }

    public CEPRule setRuleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleName() {
        return ruleName;
    }

    public CEPRule setRuleName(String ruleName) {
        this.ruleName = ruleName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CEPRule setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CEPRule setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public int getTimeWindowSeconds() {
        return timeWindowSeconds;
    }

    public CEPRule setTimeWindowSeconds(int timeWindowSeconds) {
        this.timeWindowSeconds = timeWindowSeconds;
        return this;
    }

    public List<PatternStep> getPattern() {
        return pattern;
    }

    public CEPRule setPattern(List<PatternStep> pattern) {
        this.pattern = pattern;
        return this;
    }

    public String getAction() {
        return action;
    }

    public CEPRule setAction(String action) {
        this.action = action;
        return this;
    }

    public Map<String, Object> getActionBody() {
        return actionBody;
    }

    public CEPRule setActionBody(Map<String, Object> actionBody) {
        this.actionBody = actionBody;
        return this;
    }
}