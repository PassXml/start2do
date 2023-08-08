package org.start2do.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.Json;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.start2do.eventbus.dto.EventBusConfig;
import org.start2do.eventbus.dto.EventBusDto;

@Slf4j
@Import(EventBusConfig.class)
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.eventbus", name = "enable", havingValue = "true")
public class EventBus implements MessageCodec<EventBusDto, EventBusDto> {

    private final EventBusConfig config;
    private Vertx vertx;
    private static EventBus eventBus;

    public void initializeVertx() {
        vertx = Vertx.vertx(
            new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(false)));
    }

    public void initializeClusteredVertx() throws IOException {
        ZookeeperClusterManager mgr = new ZookeeperClusterManager(
            Buffer.buffer(Files.readAllBytes(Paths.get(config.getConfigUrl()))).toJsonObject());
        vertx = Vertx.clusteredVertx(
            new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(false))).result();
    }

    @PostConstruct
    public void init_() throws IOException {
        switch (config.getType()) {
            case local -> initializeVertx();
            case Cluster -> initializeClusteredVertx();
        }
        vertx.exceptionHandler(event -> {
            log.error(event.getMessage(), event);
        });
        vertx.eventBus().registerDefaultCodec(EventBusDto.class, this);
        EventBus.eventBus = this;
    }

    public static <T> void subscribe(String address, Handler<Message<T>> handler) {
        eventBus.vertx.eventBus().consumer(address, handler);
    }

    public static <T> void sendMessage(String address, T message, Handler<T> success, Handler<Throwable> fail) {
        eventBus.vertx.eventBus().request(address, message, new DeliveryOptions().setSendTimeout(10000),
            (Handler<AsyncResult<Message<T>>>) event -> {
                if (event.succeeded()) {
                    Message<T> result = event.result();
                    T dto = result.body();
                    if (dto instanceof EventBusDto) {
                        ((EventBusDto) dto).setAddress(result.address());
                        ((EventBusDto) dto).setReplyAddress(result.replyAddress());
                    }
                    success.handle(dto);
                } else {
                    fail.handle(event.cause());
                }
            });
    }

    public void encodeToWire(Buffer buffer, EventBusDto dto) {
        buffer.appendBuffer(Json.CODEC.toBuffer(dto));
    }

    @Override
    public EventBusDto decodeFromWire(int pos, Buffer buffer) {
        return Json.decodeValue(buffer.getBuffer(0, buffer.length()), EventBusDto.class);
    }

    @Override
    public EventBusDto transform(EventBusDto dto) {
        return dto;
    }

    @Override
    public String name() {
        return EventBusDto.class.getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
