package org.start2do.script.util.impl.functions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@UtilityClass
public class HttpUtil {

    private OkHttpClient client = new OkHttpClient.Builder().cookieJar(CookieJar.NO_COOKIES)
        .hostnameVerifier((hostname, session) -> true).build();

    public void reCreateClient(int connectTimeout, int readTimeout, int writeTimeout) {
        client = new OkHttpClient.Builder().cookieJar(CookieJar.NO_COOKIES)
            .connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS).hostnameVerifier((hostname, session) -> true).build();
    }

    public void reCreateClient(OkHttpClient.Builder builder) {
        client = builder.build();
    }

    public Response get(String url) {
        return get(url, null);
    }

    public Response get(String url, Map<String, String> header) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        return executor(builder.get().url(url).build());
    }

    public Response postForm(String url, Map<String, String> header, String data) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        return executor(builder.post(RequestBody.create(Objects.requireNonNullElse(data, ""),
                MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")))
            .url(url).build());
    }

    public String bodyString(Response response) {
        try {
            return response.body().string();
        } catch (IOException e) {
            log.error("转化body.string()失败,{}", e.getMessage());
        }
        return null;
    }

    public InputStream bodyJsonNode(Response response) {
        return response.body().byteStream();
    }

    private Response executor(Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            log.error("请求失败:{}", e.getMessage());
        }
        return null;
    }

    public Response postJson(String url, Map<String, String> header, String data) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        return executor(
            builder.post(RequestBody.create(Objects.requireNonNullElse(data, ""), mediaType)).url(url).build());
    }
}
