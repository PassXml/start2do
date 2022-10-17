package org.start2do.util.spring;


import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.Json;
import io.vertx.core.spi.logging.LogDelegate;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.HttpContext;
import io.vertx.ext.web.client.impl.WebClientInternal;
import java.util.StringJoiner;

public abstract class AbsWebClient implements Handler<HttpContext<?>> {

    protected LogDelegate log = getLog();
    protected final Vertx vertx;
    protected WebClientInternal client;

    public WebClientInternal getClient() {
        return client;
    }

    public WebClientOptions getOptions() {
        return new WebClientOptions().setKeepAlive(true).setUserAgent("Time Goes by");
    }

    public abstract LogDelegate getLog();

    public AbsWebClient(Vertx vertx) {
        this.vertx = vertx;
        if (vertx.exceptionHandler() == null) {
            vertx.exceptionHandler(event -> {
                log.error(event.getMessage(), event);
            });
        }
        this.client = (WebClientInternal) WebClient.create(vertx, getOptions());
        this.client.addInterceptor(this);
    }

    protected String headerToString(MultiMap map) {
        StringJoiner joiner = new StringJoiner(",");
        map.forEach((s, s2) -> {
            joiner.add(String.format("%s=%s", s, s2));
        });
        return joiner.toString();
    }

    protected String requestStr = "request_str";

    @Override
    public void handle(HttpContext<?> ctx) {
        switch (ctx.phase()) {
            case SEND_REQUEST:
                HttpClientRequest request = ctx.clientRequest();
                String value = String.format("URL:%s %s:%d%s ,Header:[%s] Request:%s ", request.getMethod(),
                    request.getHost(),
                    request.getPort(), request.getURI(),
                    headerToString(ctx.request().headers()),
                    Json.encode(ctx.body()));
                ctx.set(requestStr, value);
                break;
            case DISPATCH_RESPONSE:
                HttpResponse<?> response = ctx.response();
                String string = (String) ctx.get(requestStr);
                log.info("{} ResponseHeader:{} ,ResponseStatus:{} ,ResponseBody:{}", string,
                    headerToString(response.headers()), response.statusCode(), response.body());
                break;
        }
        ctx.next();
    }
}


