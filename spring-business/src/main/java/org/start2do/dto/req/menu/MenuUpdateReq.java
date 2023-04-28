package org.start2do.dto.req.menu;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class MenuUpdateReq extends MenuAddReq {

    @NotNull
    private Integer id;
}
