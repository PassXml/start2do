package org.start2do.cep.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CEPResultTest {

    @Test
    void testResultCreation() {
        CEPResult result = new CEPResult();
        assertNotNull(result);
    }

    @Test
    void testResultBuilderPattern() {
        Event event1 = new Event("event1", "login_failure", "auth_service", 
                Map.of("userId", "user123", "status", "failed"), LocalDateTime.now());
        Event event2 = new Event("event2", "login_failure", "auth_service", 
                Map.of("userId", "user123", "status", "failed"), LocalDateTime.now().plusSeconds(5));

        CEPResult result = new CEPResult()
                .setRuleId("test_rule_001")
                .setRuleName("测试规则")
                .setDescription("这是一个测试规则")
                .setMatchedEvents(List.of(event1, event2))
                .setContext(Map.of("totalEvents", 2, "timeWindow", 60))
                .setMatchedAt(System.currentTimeMillis());

        assertEquals("test_rule_001", result.getRuleId());
        assertEquals("测试规则", result.getRuleName());
        assertEquals("这是一个测试规则", result.getDescription());
        assertNotNull(result.getMatchedEvents());
        assertEquals(2, result.getMatchedEvents().size());
        assertNotNull(result.getContext());
        assertEquals(2, result.getContext().get("totalEvents"));
        assertEquals(60, result.getContext().get("timeWindow"));
        assertTrue(result.getMatchedAt() > 0);
    }

    @Test
    void testResultWithEmptyLists() {
        CEPResult result = new CEPResult()
                .setRuleId("empty_test")
                .setRuleName("空列表测试")
                .setDescription("测试空列表处理")
                .setMatchedEvents(List.of())
                .setContext(Map.of());

        assertNotNull(result.getMatchedEvents());
        assertTrue(result.getMatchedEvents().isEmpty());
        assertNotNull(result.getContext());
        assertTrue(result.getContext().isEmpty());
    }
}