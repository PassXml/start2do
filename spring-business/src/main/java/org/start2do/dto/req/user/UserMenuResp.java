package org.start2do.dto.req.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserMenuResp {

    private String id;
    private String username;
    private String realName;

    public UserMenuResp(String id, String username, String realName) {
        this.id = id;
        this.username = username;
        this.realName = realName;
    }
}
