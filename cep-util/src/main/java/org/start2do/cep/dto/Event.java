package org.start2do.cep.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    /**
     * 事件来源，用于标识事件的产生方，例如：服务名、设备ID等
     */
    private String source;
    private Map<String, Object> payload;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public Event(String eventId, String eventType, String source,
        Map<String, Object> payload, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.source = source;
        this.payload = payload;
        this.timestamp = timestamp;
    }
}
