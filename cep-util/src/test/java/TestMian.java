import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.start2do.cep.dto.Event;

// 5. 完整的测试示例
public class TestMian {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); // 设置并行度为1便于调试

        // 创建测试数据源
        DataStream<Event> eventStream = env.fromElements(
            new Event("event1", "CameraHeartbeat", "deviceCode", Map.of(), LocalDateTime.now()),
            new Event("event2", "CameraHeartbeat", "deviceCode", Map.of(), LocalDateTime.now().plusSeconds(10)),
            new Event("event3", "CameraHeartbeat", "deviceCode", Map.of(), LocalDateTime.now().plusSeconds(20))
        );

        // 打印输入数据
        eventStream.keyBy(Event::getSource).print("INPUT");

        Pattern<Event, ?> pattern = Pattern.<Event>begin("start").optional();

        PatternStream<Event> patternStream = CEP.pattern(eventStream, pattern);

        SingleOutputStreamOperator<String> result = patternStream.select(
            new PatternSelectFunction<Event, String>() {
                @Override
                public String select(Map<String, List<Event>> match) throws Exception {
                    List<Event> events = match.get("start");
                    return "Matched: " + events.get(0).getEventId();
                }
            }
        );

        result.print("OUTPUT");

        env.execute("CEP Debug Job");
    }
}
