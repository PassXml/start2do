package org.start2do.cep.dto;

import java.util.List;
import java.util.Map;

public class CEPResult {
    private String ruleId;
    private String ruleName;
    private String description;
    private List<Event> matchedEvents;
    private Map<String, Object> context;
    private long matchedAt;

    public String getRuleId() {
        return ruleId;
    }

    public CEPResult setRuleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleName() {
        return ruleName;
    }

    public CEPResult setRuleName(String ruleName) {
        this.ruleName = ruleName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CEPResult setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<Event> getMatchedEvents() {
        return matchedEvents;
    }

    public CEPResult setMatchedEvents(List<Event> matchedEvents) {
        this.matchedEvents = matchedEvents;
        return this;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public CEPResult setContext(Map<String, Object> context) {
        this.context = context;
        return this;
    }

    public long getMatchedAt() {
        return matchedAt;
    }

    public CEPResult setMatchedAt(long matchedAt) {
        this.matchedAt = matchedAt;
        return this;
    }
}