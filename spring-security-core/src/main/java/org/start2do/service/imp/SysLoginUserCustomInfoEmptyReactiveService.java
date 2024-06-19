package org.start2do.service.imp;

import java.util.Map;
import org.start2do.service.reactive.ISysLoginUserCustomInfoReactiveService;
import reactor.core.publisher.Mono;


public class SysLoginUserCustomInfoEmptyReactiveService implements ISysLoginUserCustomInfoReactiveService {

    @Override
    public Mono<Map<String, Object>> getUserExtInfo(Integer userId) {
        return Mono.just(Map.of());
    }
}
