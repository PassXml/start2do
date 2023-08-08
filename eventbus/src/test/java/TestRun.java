import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.start2do.eventbus.EventBus;
import org.start2do.eventbus.dto.EventBusConfig;
import org.start2do.eventbus.dto.EventBusDto;

public class TestRun {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        EventBus eventBus = new EventBus(new EventBusConfig());
        eventBus.init_();
        String address = "100.0.0.1";
        eventBus.<EventBusDto>subscribe(address, event -> {
            event.reply(event.body());
        });
        EventBusDto message = new EventBusDto(Map.of("1", "2"));
        eventBus.sendMessage(address, message, event -> {
            try {
                System.out.println(objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, Throwable::printStackTrace);
    }
}
