package org.start2do.dto.req.dict;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictAddReq {

    private String name;
    private String type;

    private String dictName;
    private String dictType;
    private String dictId;
    private String dictDesc;
    private String dictNote;
}
