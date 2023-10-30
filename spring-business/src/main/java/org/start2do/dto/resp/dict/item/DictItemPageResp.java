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
public class DictItemPageResp {

    private UUID id;


    private String itemName;

    private String itemData;

    private Integer itemSort;
    private String itemDesc;
    private String itemNote;
    private String itemTag;


    public DictItemPageResp(String itemName, String itemData, Integer itemSort) {
        this.itemName = itemName;
        this.itemData = itemData;
        this.itemSort = itemSort;
    }
}
