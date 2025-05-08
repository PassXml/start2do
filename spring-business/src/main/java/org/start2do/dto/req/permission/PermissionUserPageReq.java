package org.start2do.dto.req.permission;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.Page;

@Setter
@Getter
@Accessors(chain = true)
public class PermissionUserPageReq extends Page {

    private String userId;
}
