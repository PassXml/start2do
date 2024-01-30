package org.start2do.service;

import java.util.Map;
import reactor.core.publisher.Mono;

public interface ISysLoginUserCustomInfoReactiveService {

    Mono<Map<String, Object>> getCustomInfo(Integer userId);
}
