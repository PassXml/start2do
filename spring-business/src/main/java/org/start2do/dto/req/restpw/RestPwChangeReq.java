package org.start2do.dto.req.restpw;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class RestPwChangeReq {

    @NotEmpty
    private String username;
    @NotEmpty
    private String newPassword;
    @NotEmpty
    private String verificationCode;

}
