package org.start2do.dto.req.login;

import java.io.Serializable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class JwtRequest implements Serializable, IPasswordText {

    private static final long serialVersionUID = 5926468583005150707L;

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    private String kaptchaKey;
    private String kaptchaCode;

    @Override
    public String getPassword_() {
        return this.password;
    }

    @Override
    public void setPassword_(String password) {
        this.password = password;
    }
}
