package org.start2do.dto.req.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.entity.security.SysUser;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserStatusReq {

    private Integer id;
    private SysUser.Status type;
}
