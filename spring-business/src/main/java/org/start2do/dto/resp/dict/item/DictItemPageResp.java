package org.start2do.dto.resp.dict.item;

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

    private String itemName;

    private String itemData;

    private Integer itemSort;

    public DictItemPageResp(String itemName, String itemData, Integer itemSort) {
        this.itemName = itemName;
        this.itemData = itemData;
        this.itemSort = itemSort;
    }
}
