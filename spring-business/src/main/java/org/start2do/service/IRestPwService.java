package org.start2do.service;

import jakarta.validation.constraints.NotEmpty;

public interface IRestPwService {


    void sendValidateEmailCode(@NotEmpty String username, String email);

    void sendValidateSMSCode(@NotEmpty String username, String phone);

    void validateCode(@NotEmpty String username, @NotEmpty String verificationCode);
}
