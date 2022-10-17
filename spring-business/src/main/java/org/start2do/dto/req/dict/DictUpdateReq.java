package org.start2do.dto.req.dict;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictUpdateReq extends DictAddReq {

    @NotNull
    private UUID id;
}
