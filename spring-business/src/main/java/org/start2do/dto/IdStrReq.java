package org.start2do.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class IdStrReq {

    @NotEmpty
    @JsonAlias({"uuid", "UUID"})
    private String id;
}
