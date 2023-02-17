package org.start2do.dto.resp.role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class RoleUsersResp {

    private Integer id;
    private String name;
    private String realName;

    public RoleUsersResp(Integer id, String name, String realName) {
        this.id = id;
        this.name = name;
        this.realName = realName;
    }
}
