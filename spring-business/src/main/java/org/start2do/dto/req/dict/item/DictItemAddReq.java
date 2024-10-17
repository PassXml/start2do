package org.start2do.dto.req.dict.item;

import java.util.UUID;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictItemAddReq {

    @NotNull
    private String dictId;
    @NotEmpty
    private String itemName;
    @NotEmpty
    private String itemData;


    private String itemTag;

    private String itemNote;

    private Integer itemSort = 0;

    private String itemDesc;
}
