package org.start2do.service;


import javax.validation.constraints.NotEmpty;

public interface IRestPwService {


    /**
     * @param username 用户名
     * @param email    电子邮件
     */
    void sendValidateEmailCode(@NotEmpty String username, String email);

    /**
     * @param username 用户名
     * @param phone    手机号码
     */
    void sendValidateSMSCode(@NotEmpty String username, String phone);

    /**
     * @param username         用户名
     * @param verificationCode 校验码
     */
    void validateCode(@NotEmpty String username, @NotEmpty String verificationCode);
}
