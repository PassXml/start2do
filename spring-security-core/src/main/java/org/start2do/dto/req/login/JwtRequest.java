package org.start2do.dto.req.login;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class JwtRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    private String kaptchaKey;
    private String kaptchaCode;

}
