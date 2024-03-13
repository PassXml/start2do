package org.start2do.service.imp;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.stereotype.Service;
import org.start2do.service.reactive.ISysLoginUserCustomInfoReactiveService;
import reactor.core.publisher.Mono;

@Service
@ConditionalOnProperty(name = "jwt.enable", havingValue = "true")
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnMissingBean(ISysLoginUserCustomInfoReactiveService.class)
public class SysLoginUserCustomInfoEmptyReactiveService implements ISysLoginUserCustomInfoReactiveService {

    @Override
    public Mono<Map<String, Object>> getCustomInfo(Integer userId) {
        return Mono.just(Map.of());
    }
}
