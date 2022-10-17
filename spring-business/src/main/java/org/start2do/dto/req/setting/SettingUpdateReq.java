package org.start2do.dto.req.setting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SettingUpdateReq extends SettingAddReq {

    private Integer id;

}
