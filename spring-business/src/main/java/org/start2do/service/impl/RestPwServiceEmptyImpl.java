package org.start2do.service.impl;

import org.start2do.service.IRestPwService;

public class RestPwServiceEmptyImpl implements IRestPwService {

    @Override
    public void sendValidateEmailCode(String username, String email) {

    }

    @Override
    public void sendValidateSMSCode(String username, String phone) {

    }

    @Override
    public void validateCode(String username, String verificationCode) {

    }
}
