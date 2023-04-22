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
public class ZLMediaOnStreamNoneReaderReq {

    /**
     * mediaServerId
     */
    private String mediaServerId;
    /**
     * app
     */
    private String app;
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
