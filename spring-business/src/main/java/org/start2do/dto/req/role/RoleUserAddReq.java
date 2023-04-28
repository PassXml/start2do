package org.start2do.dto.req.role;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class RoleUserAddReq {

    private List<Integer> userId;
    @NotNull
    private Integer roleId;
}
