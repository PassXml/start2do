package org.start2do.dto.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SettingAddReq {

    private String enable;
    private String remark;
    private String type;
    private String key;
    private String value;
    private Integer sort;

    public EnableType getEnable() {
        return EnableType.find(enable);
    }
}
