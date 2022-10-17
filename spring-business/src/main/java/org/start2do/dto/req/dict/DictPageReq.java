package org.start2do.dto.req.dict;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictPageReq {

    private String name;
    private String type;

    public void setDictName(String name) {
        this.name = name;
    }

    public void setDictType(String type) {
        this.type = type;
    }
}
