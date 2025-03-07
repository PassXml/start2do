package org.start2do.util.spring;

import java.util.Map;
import reactor.core.publisher.Mono;

public class ReactiveRequestContextHolder {
    private static final Class<Map<String, Object>> CONTEXT_KEY = (Class<Map<String, Object>>) (Class<?>) Map.class;

    private static final String KEY = "REQUEST_CONTEXT";

    public static Mono<Map<String, Object>> getContext() {
        return Mono.deferContextual(contextView ->
            Mono.just(contextView.get(CONTEXT_KEY)));
    }

    public static <T> Mono<T> withContext(Map<String, Object> context, Mono<T> mono) {
        return mono.contextWrite(ctx -> ctx.put(CONTEXT_KEY, context));
    }
}
