package org.start2do.entity;

import io.ebean.Model;
import io.ebean.annotation.DbName;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@DbName("ClickHouseDatabase")
public class HttpLog extends Model {

    public HttpLog() {
        super("ClickHouseDatabase");
    }

    @EmbeddedId
    private HttpLogId id;
    @Column(name = "create_time")
    private LocalDateTime createTime;
    private String routeId;
    private String host;
    private String clientIp;
    private String balancerIp;
    private String body;
    @Column(name = "@timestamp")
    private Integer timestamp;
    private String uri;
    private String requestHeader;
    private String reqeustQuery;
    private String reqeustBody;
    private String responseHeader;
    private String responseBody;
    private Integer responseStatus;
    private Double latency;

    private Integer upstreamLatency;
}
