package org.start2do.dto.req.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.start2do.ebean.dto.EnableType;

@Data
@Accessors(chain = true)
public class PositionSetUserReq {

    @NotEmpty
    private String positionId;
    private List<String> userId;
}
