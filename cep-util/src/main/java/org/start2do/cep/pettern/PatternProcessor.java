package org.start2do.cep.pettern;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;
import org.springframework.stereotype.Component;
import org.start2do.cep.dto.CEPRule;
import org.start2do.cep.dto.Event;
import org.start2do.cep.dto.PatternMatchResult;
import org.start2do.cep.service.RuleManagementService;
import org.start2do.cep.util.ELUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatternProcessor {

    private final RuleManagementService ruleManagementService;

    public DataStream<PatternMatchResult> processAllActivePatterns(DataStream<Event> eventStream) {
        List<CEPRule> activeRules = ruleManagementService.getAllActiveRules();
        List<DataStream<PatternMatchResult>> resultStreams = new ArrayList<>();

        if (activeRules.isEmpty()) {
            log.warn("没有活动的CEP规则，将不会处理任何模式。");
            // 返回一个永远不会有数据的流，以避免下游出现问题
            return eventStream.map(event -> null).returns(PatternMatchResult.class).filter(Objects::nonNull);
        }

        // 优化：对事件流只进行一次keyBy操作
        DataStream<Event> keyedStream = eventStream.keyBy(Event::getSource);

        for (CEPRule rule : activeRules) {
            try {
                // 将已经keyed的流传递给模式处理器
                DataStream<PatternMatchResult> resultStream = processPattern(keyedStream, rule);
                resultStreams.add(resultStream);
                log.info("已成功为规则 [{}] 构建处理流程。", rule.getRuleId());
            } catch (Exception e) {
                log.error("处理规则失败: ruleId={}", rule.getRuleId(), e);
            }
        }

        if (resultStreams.isEmpty()) {
            // 如果所有规则都构建失败，也返回一个空流
            return eventStream.map(event -> null).returns(PatternMatchResult.class).filter(Objects::nonNull);
        }

        // 合并所有规则的结果流
        DataStream<PatternMatchResult> combinedStream = resultStreams.get(0);
        for (int i = 1; i < resultStreams.size(); i++) {
            combinedStream = combinedStream.union(resultStreams.get(i));
        }
        return combinedStream;
    }

    private DataStream<PatternMatchResult> processPattern(DataStream<Event> keyedStream, CEPRule rule) {
        Pattern<Event, ?> pattern = buildPattern(rule);
        OutputTag<PatternMatchResult> timeoutTag = new OutputTag<>("timeout-" + rule.getRuleId()){};

        // 在已经keyed的流上应用CEP模式
        SingleOutputStreamOperator<PatternMatchResult> resultStream = CEP.pattern(keyedStream, pattern)
            .process(new PatternMatchFunction(rule));

        // 如果需要处理超时事件，可以从这里获取
        // DataStream<PatternMatchResult> timeoutStream = resultStream.getSideOutput(timeoutTag);

        return resultStream;
    }

    private Pattern<Event, ?> buildPattern(CEPRule rule) {
        List<CEPRule.PatternStep> steps = rule.getPattern();
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("规则必须至少包含一个模式步骤: " + rule.getRuleId());
        }

        CEPRule.PatternStep firstStep = steps.get(0);
        Pattern<Event, ?> pattern = Pattern.<Event>begin(firstStep.getStepName())
            .where(new FilterCondition(firstStep.getEventType(), firstStep.getFilters()));

        // 应用量词
        applyQuantifier(pattern, firstStep);

        Pattern<Event, ?> currentPattern = pattern;
        for (int i = 1; i < steps.size(); i++) {
            CEPRule.PatternStep currentStep = steps.get(i);
            currentPattern = currentPattern.next(currentStep.getStepName())
                .where(new FilterCondition(currentStep.getEventType(), currentStep.getFilters()));
            // 应用量词
            applyQuantifier(currentPattern, currentStep);
        }

        if (rule.getTimeWindowSeconds() > 0) {
            return currentPattern.within(Duration.ofSeconds(rule.getTimeWindowSeconds()));
        }

        return currentPattern;
    }

    private void applyQuantifier(Pattern<Event, ?> pattern, CEPRule.PatternStep step) {
        if (step.getQuantifier() == null) return;
        switch (step.getQuantifier()) {
            case ONE_OR_MORE:
                pattern.oneOrMore();
                break;
            case OPTIONAL:
                pattern.optional();
                break;
            // 其他量词可以根据需要添加
            default:
                break;
        }
    }

    private static class FilterCondition extends SimpleCondition<Event> {
        private static final long serialVersionUID = 1L;
        private final String expectedEventType;
        private final Map<String, Object> filters;

        public FilterCondition(String eventType, Map<String, Object> filters) {
            this.expectedEventType = eventType;
            this.filters = filters;
        }

        @Override
        public boolean filter(Event event) {
            if (!Objects.equals(event.getEventType(), this.expectedEventType)) {
                return false;
            }
            if (filters == null || filters.isEmpty()) {
                return true;
            }
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String field = entry.getKey();
                Object expected = entry.getValue();
                Object actual = event.getPayload().get(field);

                if (actual == null) return false;

                if (expected instanceof String expectedStr && expectedStr.startsWith("#{")) {
                    if (!ELUtil.evaluate(expectedStr, actual)) return false;
                } else if (!Objects.equals(actual, expected)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class PatternMatchFunction extends org.apache.flink.cep.functions.PatternProcessFunction<Event, PatternMatchResult> {
        private static final long serialVersionUID = 1L;
        private final CEPRule rule;

        public PatternMatchFunction(CEPRule rule) {
            this.rule = rule;
        }

        @Override
        public void processMatch(Map<String, List<Event>> match, Context ctx, org.apache.flink.util.Collector<PatternMatchResult> out) {
            PatternMatchResult result = new PatternMatchResult()
                .setRuleId(rule.getRuleId())
                .setRuleName(rule.getRuleName())
                .setAction(rule.getAction())
                .setActionBody(rule.getActionBody())
                .setMatchTimestamp(LocalDateTime.now())
                .setMatchedEvents(match);
            out.collect(result);
        }
    }
}
