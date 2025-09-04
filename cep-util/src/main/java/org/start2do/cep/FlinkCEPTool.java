package org.start2do.cep;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.legacy.SinkFunction;
import org.apache.flink.util.Collector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.start2do.cep.callback.CEPCallback;
import org.start2do.cep.callback.CustomCallbackRegistry;
import org.start2do.cep.callback.HttpCallback;
import org.start2do.cep.controller.CEPEventController;
import org.start2do.cep.dto.CEPResult;
import org.start2do.cep.dto.CEPRule;
import org.start2do.cep.dto.Event;
import org.start2do.cep.service.DynamicRuleManagementService;
import org.start2do.cep.source.HttpEventSource;
import org.start2do.cep.source.QueueEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@EnableConfigurationProperties(FlinkCEPProperties.class)
@ConditionalOnProperty(name = "flink.enabled", havingValue = "true")
public class FlinkCEPTool {
    private static final Logger log = LoggerFactory.getLogger(FlinkCEPTool.class);

    private final FlinkCEPProperties properties;
    private StreamExecutionEnvironment env;
    private DataStream<Event> eventStream;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private ScheduledExecutorService autoReloadExecutor;

    public FlinkCEPTool(FlinkCEPProperties properties) {
        this.properties = properties;
    }

    public void initialize() {
        if (initialized.compareAndSet(false, true)) {
            try {
                env = StreamExecutionEnvironment.getExecutionEnvironment();
                env.setParallelism(properties.getParallelism());

                if (properties.isEnableCheckpoint()) {
                    env.enableCheckpointing(properties.getCheckpointInterval());
                }

                setupEventStreams();
                setupCEPProcessing();

                log.info("Flink CEP Tool initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Flink CEP Tool", e);
                throw new RuntimeException("Initialization failed", e);
            }
        }
    }

    public void start() {
        if (initialized.get() && started.compareAndSet(false, true)) {
            try {
                new Thread(() -> {
                    try {
                        env.execute(properties.getJobName());
                    } catch (Exception e) {
                        log.error("Flink job execution failed", e);
                    }
                }).start();

                log.info("Flink CEP Tool started successfully");
            } catch (Exception e) {
                log.error("Failed to start Flink CEP Tool", e);
                throw new RuntimeException("Start failed", e);
            }
        }
    }

    public void stop() {
        if (started.compareAndSet(true, false)) {
            try {
                if (autoReloadExecutor != null) {
                    autoReloadExecutor.shutdown();
                }
                log.info("Flink CEP Tool stopped");
            } catch (Exception e) {
                log.error("Failed to stop Flink CEP Tool", e);
            }
        }
    }

    private void setupEventStreams() {
        DataStream<Event> queueStream = env.addSource(new QueueEventSource()).name("QueueEventSource").uid("queue-source");

        DataStream<Event> httpStream = env.addSource(new HttpEventSource()).name("HttpEventSource").uid("http-source");

        eventStream = queueStream.union(httpStream);
//                .name("CombinedEventStream")
//                .uid("combined-stream");
    }

    private void setupCEPProcessing() {
        eventStream = eventStream.assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps().withTimestampAssigner((event, timestamp) -> event.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()));

        DataStream<CEPResult> resultStream = eventStream.keyBy(Event::getSource).process(new DynamicCEPProcessFunction()).name("DynamicCEPProcessor").uid("cep-processor");

        resultStream.addSink(new CEPSinkFunction()).name("CEPSink").uid("cep-sink");
    }

    public void addRule(CEPRule rule) {
        DynamicRuleManagementService.addRule(rule);
    }

    public void addRules(List<CEPRule> rules) {
        DynamicRuleManagementService.addRules(rules);
    }

    public CEPRule updateRule(CEPRule rule) {
        return DynamicRuleManagementService.updateRule(rule);
    }

    public CEPRule deleteRule(String ruleId) {
        return DynamicRuleManagementService.deleteRule(ruleId);
    }

    public boolean setRuleEnabled(String ruleId, boolean enabled) {
        return DynamicRuleManagementService.setRuleEnabled(ruleId, enabled);
    }

    public List<CEPRule> getActiveRules() {
        return DynamicRuleManagementService.getActiveRules();
    }

    public void registerCallback(String callbackType, CEPCallback callback) {
        CustomCallbackRegistry.registerCallback(callbackType, callback);
    }

    public void unregisterCallback(String callbackType) {
        CustomCallbackRegistry.unregisterCallback(callbackType);
    }

    public void inputQueueEvent(Event event) {
        QueueEventSource.putEvent(event);
    }

    public boolean inputQueueEventWithTimeout(Event event, long timeout, TimeUnit unit) throws InterruptedException {
        return QueueEventSource.putEventWithTimeout(event, timeout, unit);
    }

    public void inputQueueEventsBatch(List<Event> events) {
        for (Event event : events) {
            QueueEventSource.putEvent(event);
        }
    }

    public void inputHttpEvent(Event event) {
        HttpEventSource.putEvent(event);
    }

    public boolean inputHttpEventWithTimeout(Event event, long timeout, TimeUnit unit) throws InterruptedException {
        return HttpEventSource.putEventWithTimeout(event, timeout, unit);
    }

    public void inputHttpEventsBatch(List<Event> events) {
        for (Event event : events) {
            HttpEventSource.putEvent(event);
        }
    }

    public int getQueueEventQueueSize() {
        return QueueEventSource.getQueueSize();
    }

    public int getHttpEventQueueSize() {
        return HttpEventSource.getQueueSize();
    }

    public void enableAutoReload(long intervalMs) {
        if (autoReloadExecutor == null) {
            autoReloadExecutor = Executors.newSingleThreadScheduledExecutor();
            autoReloadExecutor.scheduleAtFixedRate(this::reloadRules, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
            log.info("Auto reload enabled with interval: {} ms", intervalMs);
        }
    }

    public void disableAutoReload() {
        if (autoReloadExecutor != null) {
            autoReloadExecutor.shutdown();
            autoReloadExecutor = null;
            log.info("Auto reload disabled");
        }
    }

    public void reloadRules() {
        log.info("Reloading rules...");
    }

    public void addRuleChangeListener(DynamicRuleManagementService.RuleChangeListener listener) {
        DynamicRuleManagementService.addRuleChangeListener(listener);
    }

    @Bean
    public CEPEventController cepEventController() {
        return new CEPEventController();
    }

    private static class DynamicCEPProcessFunction extends org.apache.flink.streaming.api.functions.KeyedProcessFunction<String, Event, CEPResult> {
        @Override
        public void processElement(Event event, Context ctx, Collector<CEPResult> out) throws Exception {
            List<CEPRule> activeRules = DynamicRuleManagementService.getActiveRules();

            for (CEPRule rule : activeRules) {
                if (matchesRule(event, rule)) {
                    CEPResult result = new CEPResult().setRuleId(rule.getRuleId()).setRuleName(rule.getRuleName()).setDescription(rule.getDescription()).setMatchedEvents(List.of(event)).setMatchedAt(System.currentTimeMillis());

                    out.collect(result);
                }
            }
        }

        private boolean matchesRule(Event event, CEPRule rule) {
            if (rule.getPattern() == null || rule.getPattern().isEmpty()) {
                return false;
            }

            CEPRule.PatternStep firstStep = rule.getPattern().get(0);
            if (!event.getEventType().equals(firstStep.getEventType())) {
                return false;
            }

            if (firstStep.getFilters() != null && !firstStep.getFilters().isEmpty()) {
                Map<String, Object> payload = event.getPayload();
                if (payload == null) {
                    return false;
                }

                for (Map.Entry<String, Object> filter : firstStep.getFilters().entrySet()) {
                    Object payloadValue = payload.get(filter.getKey());
                    if (payloadValue == null || !payloadValue.equals(filter.getValue())) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    private static class CEPSinkFunction implements SinkFunction<CEPResult> {
        @Override
        public void invoke(CEPResult result, Context context) throws Exception {
            try {
                CEPRule rule = DynamicRuleManagementService.getRule(result.getRuleId());
                if (rule != null) {
                    String action = rule.getAction();
                    Map<String, Object> actionBody = rule.getActionBody();

                    if ("http_callback".equals(action) && actionBody != null) {
                        String callbackUrl = (String) actionBody.get("callbackUrl");
                        String httpMethod = (String) actionBody.getOrDefault("httpMethod", "POST");
                        Map<String, String> headers = (Map<String, String>) actionBody.get("headers");

                        HttpCallback httpCallback = new HttpCallback(callbackUrl, httpMethod, headers);
                        httpCallback.execute(result);
                    } else if ("custom_callback".equals(action) && actionBody != null) {
                        String callbackType = (String) actionBody.get("callbackType");
                        CustomCallbackRegistry.executeCallback(callbackType, result);
                    }
                }

                log.info("CEP result processed: {}", result.getRuleId());
            } catch (Exception e) {
                log.error("Failed to process CEP result: " + result.getRuleId(), e);
            }
        }
    }
}