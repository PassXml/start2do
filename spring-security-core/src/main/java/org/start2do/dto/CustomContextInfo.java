package org.start2do.dto;

import org.start2do.dto.req.login.IPasswordText;
import org.start2do.dto.req.login.JwtRequest;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public interface CustomContextInfo {

    void loadReqBefore(JwtRequest request);
    <R> Mono<R> loadUserBefore(Mono<R> mono);

    Context injectContext(Context context, String jwtStr, Integer tenantId, Object otherInfo);

    Mono<Object> injectOtherInfo(String jwtStr);

    void loadReqBefore(IPasswordText req);
}
