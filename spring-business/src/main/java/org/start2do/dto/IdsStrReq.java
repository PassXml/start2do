package org.start2do.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class IdsStrReq {

    @NotEmpty
    @JsonAlias({"uuid", "UUID", "ids"})
    private List<String> id;
}
