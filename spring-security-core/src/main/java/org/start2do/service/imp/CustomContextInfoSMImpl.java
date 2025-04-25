package org.start2do.service.imp;

import lombok.RequiredArgsConstructor;
import org.start2do.Start2doSecurityConfig.LoginPasswordEncryptConfig;
import org.start2do.dto.CustomContextInfo;
import org.start2do.dto.req.login.IPasswordText;
import org.start2do.dto.req.login.JwtRequest;
import org.start2do.util.SM2Util;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;


@RequiredArgsConstructor
public class CustomContextInfoSMImpl implements CustomContextInfo {

    private final LoginPasswordEncryptConfig config;

    @Override
    public void loadReqBefore(JwtRequest req) {
        req.setPassword(SM2Util.decrypt(config.getSecret(), req.getPassword()));
    }

    @Override
    public <R> Mono<R> loadUserBefore(Mono<R> mono) {
        return null;
    }

    @Override
    public Context injectContext(Context context, String jwtStr, Integer tenantId, Object otherInfo) {
        return null;
    }

    @Override
    public Mono<Object> injectOtherInfo(String jwtStr) {
        return null;
    }

    @Override
    public void loadReqBefore(IPasswordText req) {
        req.setPassword_(SM2Util.decrypt(config.getSecret(), req.getPassword_()));
    }
}
