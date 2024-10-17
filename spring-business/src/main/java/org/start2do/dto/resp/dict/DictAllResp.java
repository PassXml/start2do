package org.start2do.dto.resp.dict;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.resp.dict.item.DictItemPageResp;

@Data
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class DictAllResp {

    @JsonIgnore
    private String id;
    private String dictName;
    private String dictDesc;

    private List<DictItemPageResp> items;

    public DictAllResp(String dictName, List<DictItemPageResp> items) {
        this.dictName = dictName;
        this.items = items;
    }

    public DictAllResp(String dictName, String dictDesc, List<DictItemPageResp> items) {
        this.dictName = dictName;
        this.dictDesc = dictDesc;
        this.items = items;
    }
}
