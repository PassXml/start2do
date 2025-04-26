package org.start2do.service;

import java.util.Map;
import reactor.core.publisher.Mono;

public interface ISysLoginUserCustomInfoService {

    Map<String, Object> getCustomInfo(Integer userId);

    Mono<Map<String, Object>> getCustomInfoReactive(Integer userId);

}
