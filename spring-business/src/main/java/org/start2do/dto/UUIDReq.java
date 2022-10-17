package org.start2do.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class UUIDReq {

    /**
      *  UUID  直接传UUID的String即可
     */
    @NotNull
    @JsonAlias({"uuid", "UUID"})
    private UUID id;
}
