package org.start2do.dto.req.dict.item;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictItemUpdateReq extends DictItemAddReq {

    @NotNull
    private String id;
}
