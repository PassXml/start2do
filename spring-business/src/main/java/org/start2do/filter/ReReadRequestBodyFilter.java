package org.start2do.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "start2do.business.sys-log", value = "enable", havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReReadRequestBodyFilter implements WebFilter, Ordered {

    /**
     * 是否处理
     */
    public static boolean isHandle(ServerHttpRequest request) {
        return MediaType.APPLICATION_JSON_VALUE.equals(request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        ServerHttpRequest request = exchange.getRequest();
//        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
//        // 只对contentType=application/json的数据进行重写RequesetBody
//        if (MediaType.APPLICATION_JSON_VALUE.equals(contentType) && request.getMethod().matches("POST")) {
//            Flux<DataBuffer> body = request.getBody();
//            return DataBufferUtils.join(body).flatMap(dataBuffer -> {
//                Flux<DataBuffer> cachedFlux = Flux.defer(
//                    () -> {
//
//                        return Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount()));
//                    });
//                //封装request，传给下一级
//                ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
//                    @Override
//                    public Flux<DataBuffer> getBody() {
//                        return cachedFlux;
//                    }
//                };
//                return chain.filter(exchange.mutate().request(mutatedRequest).build())
//                    .doFinally(signalType -> {
//                        dataBuffer.readableByteCount();
//                    });
//            }).switchIfEmpty(chain.filter(exchange));
//        }
//        return chain.filter(exchange);
        ServerHttpRequest request = exchange.getRequest();
        Flux<DataBuffer> body = request.getBody();
        if (isHandle(request)) {
            return DataBufferUtils.join(body).map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return bytes;
            }).map(bytes -> {
                ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                    }
                };
                ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
                return chain.filter(modifiedExchange);
            }).flatMap(result -> result).switchIfEmpty(
                chain.filter(exchange)
            );
        }
        return chain.filter(exchange);
    }
}
