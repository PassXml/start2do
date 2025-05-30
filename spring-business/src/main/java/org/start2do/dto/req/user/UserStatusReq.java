package org.start2do.dto.req.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.entity.security.SysUser;
import org.start2do.util.validator.InEnumValue;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserStatusReq {

    private String id;
    @InEnumValue(SysUser.Status.class)
    private String type;

    public SysUser.Status getType() {
        return SysUser.Status.find(type);
    }
}
