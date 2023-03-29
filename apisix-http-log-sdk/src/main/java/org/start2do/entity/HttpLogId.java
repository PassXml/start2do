package org.start2do.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Embeddable
public class HttpLogId implements Serializable {

    @Column(name = "create_time")
    private LocalDateTime createTime;
    private String routeId;
}
