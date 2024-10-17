package org.start2do.dto.resp.dict.item;

import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictItemDetailResp {

    private String id;
    private String dictId;

    private String itemName;

    private String itemData;


    private String itemTag;

    private String itemNote;

    private Integer itemSort;

    private String itemDesc;

}
