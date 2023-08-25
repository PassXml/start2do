package org.start2do.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ZLMediaOnPlayReq {

    @JsonProperty("mediaServerId")
    private String mediaServerId;
    @JsonProperty("app")
    private String app;
    @JsonProperty("id")
    private String id;
    @JsonProperty("ip")
    private String ip;
    @JsonProperty("params")
    private String params;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("schema")
    private String schema;
    @JsonProperty("stream")
    private String stream;
    @JsonProperty("vhost")
    private String vhost;
}
