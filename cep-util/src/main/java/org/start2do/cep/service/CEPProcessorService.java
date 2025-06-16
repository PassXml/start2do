package org.start2do.cep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.start2do.cep.action.IAction;
import org.start2do.cep.action.IActionMap;
import org.start2do.cep.config.FlinkConfig;
import org.start2do.cep.dto.Event;
import org.start2do.cep.dto.PatternMatchResult;
import org.start2do.cep.pettern.PatternProcessor;
import org.start2do.cep.sink.ResultSink;
import org.start2do.cep.source.HttpEventSource;
import org.start2do.cep.source.MQEventSource;

@Slf4j
@Service
@RequiredArgsConstructor
public class CEPProcessorService implements ApplicationListener<ApplicationReadyEvent> {

    private final ObjectMapper objectMapper;
    private final FlinkConfig flinkConfig;
    private final PatternProcessor patternProcessor;
    private StreamExecutionEnvironment env;
    private final IActionMap actionMap;


    /**
     * 初始化Flink环境
     */
    @PostConstruct
    public void initializeFlinkEnvironment() {
        // 创建流处理环境
        // 使用 getExecutionEnvironment() 更为通用，能自动适应不同执行环境
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置并行度
        env.setParallelism(flinkConfig.getParallelism());

        // 配置检查点
        if (flinkConfig.isEnableCheckpoint()) {
            env.enableCheckpointing(flinkConfig.getCheckpointInterval());
        }

        log.info("Flink环境初始化完成，并行度: {}", flinkConfig.getParallelism());
    }

    /**
     * 启动CEP处理任务
     */
    public void startCEPJob() throws Exception {
        log.info("启动CEP处理任务: {}", flinkConfig.getJobName());

        // 创建事件数据流
        DataStream<Event> eventStream = createEventStream();

        // 应用CEP模式处理
        DataStream<PatternMatchResult> resultStream =
            patternProcessor.processAllActivePatterns(eventStream);

        Map<String, List<IAction>> map = new ConcurrentHashMap<>();
        for (IAction action : actionMap.getActions()) {
            String key = action.type().toLowerCase();
            List<IAction> actions = map.getOrDefault(key, new ArrayList<>());
            actions.add(action);
            map.put(key, actions);
        }
        // 输出结果
        ResultSink sink = new ResultSink(map);
        resultStream.sinkTo(sink);

        // 执行任务
        env.execute(flinkConfig.getJobName());
    }

    /**
     * 创建事件数据流
     */
    private DataStream<Event> createEventStream() {
        DataStream<Event> eventStream = null;

        // HTTP数据源
        if (flinkConfig.getHttp().isEnabled()) {
            DataStream<Event> httpStream = env.addSource(new HttpEventSource(flinkConfig.getHttp()));
            eventStream = httpStream;
            log.info("已启用HTTP事件源");
        }

        // Kafka数据源
        if (flinkConfig.getKafka().isEnabled()) {
            MQEventSource mqSource = new MQEventSource(objectMapper);
            DataStream<Event> kafkaStream = mqSource.createKafkaSource(
                env,
                flinkConfig.getKafka().getBootstrapServers(),
                flinkConfig.getKafka().getTopic(),
                flinkConfig.getKafka().getGroupId()
            );

            if (eventStream == null) {
                eventStream = kafkaStream;
            } else {
                eventStream = eventStream.union(kafkaStream);
            }
            log.info("已启用Kafka事件源");
        }

        if (eventStream == null) {
            throw new IllegalStateException("至少需要启用一个数据源 (HTTP or Kafka)");
        }

        return eventStream;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Spring Boot 应用已就绪，准备启动 Flink CEP 作业...");
        // 在新线程中运行，以避免阻塞主应用线程
        new Thread(() -> {
            try {
                startCEPJob();
            } catch (Exception e) {
                log.error("Flink CEP 作业执行失败", e);
            }
        }).start();
    }
}
