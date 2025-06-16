package org.start2do.cep.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.start2do.cep.dto.Event;

@Slf4j
@RequiredArgsConstructor
public class MQEventSource {

    private final ObjectMapper objectMapper;

    public DataStream<Event> createKafkaSource(StreamExecutionEnvironment env, String bootstrapServers, String topic, String groupId) {
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
            .setBootstrapServers(bootstrapServers)
            .setTopics(topic)
            .setGroupId(groupId)
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        return env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "KafkaSource-" + topic)
            .map(this::deserialize)
            .filter(java.util.Objects::nonNull);
    }

    private Event deserialize(String json) {
        try {
            return objectMapper.readValue(json, Event.class);
        } catch (IOException e) {
            log.error("反序列化事件失败: json='{}'", json, e);
            return null;
        }
    }
}
