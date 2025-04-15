package org.start2do.dto.req.restpw;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.req.login.IPasswordText;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class RestPwChangeReq implements IPasswordText {

    @NotEmpty
    private String username;
    @NotEmpty
    private String newPassword;
    @NotEmpty
    private String verificationCode;

    @Override
    public String getPassword_() {
        return this.newPassword;
    }

    @Override
    public void setPassword_(String password) {
        this.newPassword = password;
    }
}
