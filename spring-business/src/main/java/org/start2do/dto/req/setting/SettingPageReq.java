package org.start2do.dto.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SettingPageReq extends Page {

    private String type;
    private String key;
    private String value;
}
