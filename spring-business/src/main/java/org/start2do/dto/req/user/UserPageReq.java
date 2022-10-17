package org.start2do.dto.req.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UserPageReq extends Page {

    private String username;
    private Integer role;

}
