package org.start2do.dto.req;

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
public class ZLMediaOnStreamNotFoundReq {

    /**
     * mediaServerId
     */
    private String mediaServerId;
    /**
     * app
     */
    private String app;
    /**
     * id
     */
    private String id;
    /**
     * ip
     */
    private String ip;
    /**
     * params
     */
    private String params;
    /**
     * port
     */
    private Integer port;
    /**
     * schema
     */
    private String schema;
    /**
     * stream
     */
    private String stream;
    /**
     * vhost
     */
    private String vhost;
}
