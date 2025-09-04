package org.start2do.cep.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CEPRuleTest {

    @Test
    void testRuleCreation() {
        CEPRule rule = new CEPRule();
        assertNotNull(rule);
    }

    @Test
    void testRuleBuilderPattern() {
        CEPRule.PatternStep patternStep = new CEPRule.PatternStep()
                .setStepName("first_failure")
                .setEventType("login_failure")
                .setFilters(Map.of("status", "failed"))
                .setQuantifier(CEPRule.PatternStep.PatternQuantifier.ONE);

        Map<String, Object> actionBody = Map.of("callbackUrl", "http://localhost:8080/api/alerts");

        CEPRule rule = new CEPRule()
                .setRuleId("login_failure_3_times")
                .setRuleName("连续失败登录检测")
                .setDescription("检测用户连续3次登录失败")
                .setEnabled(true)
                .setPattern(List.of(patternStep))
                .setTimeWindowSeconds(300)
                .setAction("http_callback")
                .setActionBody(actionBody);

        assertEquals("login_failure_3_times", rule.getRuleId());
        assertEquals("连续失败登录检测", rule.getRuleName());
        assertEquals("检测用户连续3次登录失败", rule.getDescription());
        assertTrue(rule.isEnabled());
        assertEquals(300, rule.getTimeWindowSeconds());
        assertEquals("http_callback", rule.getAction());
        assertNotNull(rule.getPattern());
        assertNotNull(rule.getActionBody());
    }

    @Test
    void testPatternStepBuilderPattern() {
        CEPRule.PatternStep step = new CEPRule.PatternStep()
                .setStepName("test_step")
                .setEventType("test_event")
                .setFilters(Map.of("key", "value"))
                .setQuantifier(CEPRule.PatternStep.PatternQuantifier.ONE_OR_MORE);

        assertEquals("test_step", step.getStepName());
        assertEquals("test_event", step.getEventType());
        assertEquals(Map.of("key", "value"), step.getFilters());
        assertEquals(CEPRule.PatternStep.PatternQuantifier.ONE_OR_MORE, step.getQuantifier());
    }

    @Test
    void testPatternQuantifierValues() {
        CEPRule.PatternStep.PatternQuantifier[] quantifiers = CEPRule.PatternStep.PatternQuantifier.values();
        assertEquals(5, quantifiers.length);
        
        assertTrue(List.of(quantifiers).contains(CEPRule.PatternStep.PatternQuantifier.ONE));
        assertTrue(List.of(quantifiers).contains(CEPRule.PatternStep.PatternQuantifier.ONE_OR_MORE));
        assertTrue(List.of(quantifiers).contains(CEPRule.PatternStep.PatternQuantifier.ZERO_OR_MORE));
        assertTrue(List.of(quantifiers).contains(CEPRule.PatternStep.PatternQuantifier.ZERO_OR_ONE));
        assertTrue(List.of(quantifiers).contains(CEPRule.PatternStep.PatternQuantifier.TIMES));
    }
}