package org.start2do.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SqlPagePojo {

    private String bodyKey;
    private String bodyValue;

    public String getBodyValueLike() {
        return "%" + bodyValue + "%";
    }
}
