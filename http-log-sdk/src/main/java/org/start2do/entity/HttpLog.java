package org.start2do.entity;

import io.ebean.Model;
import io.ebean.annotation.DbName;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.EbeanClickHouseAutoConfig;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@DbName(EbeanClickHouseAutoConfig.ClickHouseEbeanDatabase)
public class HttpLog extends Model {

    public HttpLog() {
        super(EbeanClickHouseAutoConfig.ClickHouseEbeanDatabase);
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
