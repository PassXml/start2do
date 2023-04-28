package org.start2do.dto.req.role;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class RoleMenuReq {

    @NotNull
    private Integer roleId;
    @JsonAlias("menuId")
    private List<Integer> menuIds;
}
