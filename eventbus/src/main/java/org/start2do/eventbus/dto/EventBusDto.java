package org.start2do.eventbus.dto;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.ClusterSerializable;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class EventBusDto<T> implements Serializable, ClusterSerializable {


    private String version = "1.0";
    private String address;
    private String replyAddress;
    private String tClassName;
    private T body;

    public EventBusDto(T body) {
        if (body == null) {
            throw new RuntimeException("body is null");
        }
        this.body = body;
        this.tClassName = body.getClass().getName();
    }

    @Override
    public void writeToBuffer(Buffer buffer) {
        buffer.appendBuffer(Json.CODEC.toBuffer(this));
    }

    @Override
    public int readFromBuffer(int pos, Buffer buffer) {
        return buffer.length();
    }
}
