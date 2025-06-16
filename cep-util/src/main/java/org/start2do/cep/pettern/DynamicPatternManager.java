package org.start2do.cep.pettern;


import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.nfa.aftermatch.SkipPastLastStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.start2do.cep.dto.CEPRule;
import org.start2do.cep.dto.Event;

public class DynamicPatternManager {

    private static final Logger logger = LoggerFactory.getLogger(DynamicPatternManager.class);

    // 存储规则和对应的模式
    private final Map<String, CEPRule> rules = new ConcurrentHashMap<>();
    private final Map<String, Pattern<Event, ?>> patterns = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // 单例模式
    private static class SingletonHolder {

        private static final DynamicPatternManager INSTANCE = new DynamicPatternManager();
    }

    public static DynamicPatternManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private DynamicPatternManager() {
    }

    /**
     * 添加或更新规则
     */
    public void addOrUpdateRule(CEPRule rule) {
        lock.writeLock().lock();
        try {
            logger.info("添加/更新规则: {}", rule.getRuleId());

            // 保存规则
            rules.put(rule.getRuleId(), rule);

            // 生成模式
            Pattern<Event, ?> pattern = buildPatternFromRule(rule);
            patterns.put(rule.getRuleId(), pattern);

            logger.info("规则 {} 已成功添加/更新", rule.getRuleId());

        } catch (Exception e) {
            logger.error("添加/更新规则失败: {}", rule.getRuleId(), e);
            throw new RuntimeException("添加/更新规则失败", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 删除规则
     */
    public void removeRule(String ruleId) {
        lock.writeLock().lock();
        try {
            logger.info("删除规则: {}", ruleId);
            rules.remove(ruleId);
            patterns.remove(ruleId);
            logger.info("规则 {} 已删除", ruleId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取规则
     */
    public CEPRule getRule(String ruleId) {
        lock.readLock().lock();
        try {
            return rules.get(ruleId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取模式
     */
    public Pattern<Event, ?> getPattern(String ruleId) {
        lock.readLock().lock();
        try {
            return patterns.get(ruleId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取所有活跃的规则
     */
    public Map<String, CEPRule> getActiveRules() {
        lock.readLock().lock();
        try {
            Map<String, CEPRule> activeRules = new ConcurrentHashMap<>();
            rules.entrySet().stream().filter(entry -> entry.getValue().isEnabled())
                .forEach(entry -> activeRules.put(entry.getKey(), entry.getValue()));
            return activeRules;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取所有活跃的模式
     */
    public Map<String, Pattern<Event, ?>> getActivePatterns() {
        lock.readLock().lock();
        try {
            Map<String, Pattern<Event, ?>> activePatterns = new ConcurrentHashMap<>();
            rules.entrySet().stream().filter(entry -> entry.getValue().isEnabled()).forEach(entry -> {
                Pattern<Event, ?> pattern = patterns.get(entry.getKey());
                if (pattern != null) {
                    activePatterns.put(entry.getKey(), pattern);
                }
            });
            return activePatterns;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 从规则构建Flink CEP模式
     */
    private Pattern<Event, ?> buildPatternFromRule(CEPRule rule) {
        List<CEPRule.PatternStep> steps = rule.getPattern();
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("规则模式不能为空");
        }

        // 开始构建模式
        Pattern<Event, Event> pattern = null;
        SkipPastLastStrategy strategy = AfterMatchSkipStrategy.skipPastLastEvent();
        for (int i = 0; i < steps.size(); i++) {
            CEPRule.PatternStep step = steps.get(i);

            if (i == 0) {
                // 第一个步骤
                pattern = Pattern.<Event>begin(step.getStepName(), strategy).where(createCondition(step));

                // 设置量词
                pattern = applyQuantifier(pattern, step);

            } else {
                // 后续步骤
                Pattern<Event, Event> nextPattern = pattern.next(step.getStepName()).where(createCondition(step));

                // 设置量词
                pattern = applyQuantifier(nextPattern, step);
            }

            // 设置超时
            if (step.getTimeoutSeconds() > 0) {
                pattern = pattern.within(Duration.ofSeconds(step.getTimeoutSeconds()));
            }
        }

        // 设置整体时间窗口
        if (rule.getTimeWindowSeconds() > 0) {
            pattern = pattern.within(Duration.ofSeconds(rule.getTimeWindowSeconds()));
        }

        // 设置跳过策略
        return pattern;
    }

    /**
     * 创建事件条件
     */
    private SimpleCondition<Event> createCondition(CEPRule.PatternStep step) {
        return new SimpleCondition<Event>() {
            @Override
            public boolean filter(Event event) throws Exception {
                // 检查事件类型
                if (!step.getEventType().equals(event.getEventType())) {
                    return false;
                }
                // 检查过滤条件
                Map<String, Object> filters = step.getFilters();
                if (filters != null && !filters.isEmpty()) {
                    return checkFilters(event, filters);
                }

                return true;
            }
        };
    }

    /**
     * 检查过滤条件
     */
    private boolean checkFilters(Event event, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String field = filter.getKey();
            Object expectedValue = filter.getValue();

            // 从事件payload中获取字段值
            Object actualValue;
            if ("source".equals(field)) {
                actualValue = event.getSource();
            } else if ("eventType".equals(field)) {
                actualValue = event.getEventType();
            } else {
                actualValue = event.getPayload().get(field);
            }

            // 比较值
            if (!compareValues(actualValue, expectedValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 值比较逻辑
     */
    private boolean compareValues(Object actual, Object expected) {
        if (actual == null && expected == null) {
            return true;
        }
        if (actual == null || expected == null) {
            return false;
        }
        // 处理字符串匹配
        if (expected instanceof String expectedStr) {
            String actualStr = actual.toString();
            // 支持通配符匹配
            if (expectedStr.contains("*")) {
                String regex = expectedStr.replace("*", ".*");
                return actualStr.matches(regex);
            }
            return actualStr.equals(expectedStr);
        }

        // 处理数值比较
        if (expected instanceof Number expected_ && actual instanceof Number actual_) {
            double expectedNum = expected_.doubleValue();
            double actualNum = actual_.doubleValue();
            return Double.compare(actualNum, expectedNum) == 0;
        }
        return actual.equals(expected);
    }

    /**
     * 应用量词
     */
    @SuppressWarnings("unchecked")
    private Pattern<Event, Event> applyQuantifier(Pattern<Event, Event> pattern, CEPRule.PatternStep step) {
        String quantifier = step.getQuantifier();
        if (quantifier == null) {
            return pattern;
        }
        switch (quantifier.toUpperCase()) {
            case "ONE_OR_MORE":
                return pattern.oneOrMore();
            case "ZERO_OR_MORE":
                return pattern.timesOrMore(0);
            case "TIMES":
                // 支持指定次数，从filters中获取times参数
                Object times = step.getFilters().get("times");
                if (times instanceof Number times_) {
                    return pattern.times(times_.intValue());
                }
                return pattern;
            default:
                // 默认为ONE
                return pattern;
        }
    }
}
