package org.start2do.dto.req.dict;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictAddReq {

    @NotEmpty
    private String dictName;
    private String dictType;
    private String dictId;
    private String dictDesc;
    private String dictNote;
}
