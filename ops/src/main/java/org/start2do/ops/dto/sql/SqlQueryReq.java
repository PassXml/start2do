package org.start2do.ops.dto.sql;

import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class SqlQueryReq {

    @NotEmpty
    private String sql;
}
