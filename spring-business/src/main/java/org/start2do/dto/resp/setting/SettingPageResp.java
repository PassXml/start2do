package org.start2do.dto.resp.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SettingPageResp {

    private Integer id;
    private String enable;
    private String enableStr;
    private String remark;
    private String type;
    private String key;
    private String value;
    private Integer sort;
}
