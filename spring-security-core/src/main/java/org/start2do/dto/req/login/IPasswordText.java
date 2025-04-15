package org.start2do.dto.req.login;

import lombok.experimental.Accessors;

@Accessors(chain = true)
public interface IPasswordText {

    String getPassword_();

    void setPassword_(String password);

}
