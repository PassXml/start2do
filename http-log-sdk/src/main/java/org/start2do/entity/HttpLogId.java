package org.start2do.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@EqualsAndHashCode
@NoArgsConstructor
@Embeddable
public class HttpLogId implements Serializable {

    @Column(name = "create_time")
    private LocalDateTime createTime;
    private String routeId;

    public HttpLogId(LocalDateTime createTime, String routeId) {
        this.createTime = createTime;
        this.routeId = routeId;
    }
}
