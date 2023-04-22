package org.start2do.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class HttpLogPageReq extends Page {

    private String bodyKey;
    private String bodyValue;
    private String uri;
    private Integer httpStatus;
}
