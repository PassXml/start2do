package org.start2do.script.util.impl.functions;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
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
            .writeTimeout(writeTimeout, TimeUnit.SECONDS).hostnameVerifier((hostname, session) -> true)
            .build();
    }

    public void reCreateClient(OkHttpClient.Builder builder) {
        client = builder.build();
    }

    public Response get(String url, Map<String, String> header) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        try {
            Response response = client.newCall(builder.get().url(url).build()).execute();
            return response;
        } catch (IOException e) {
            log.error("请求失败:{}", e.getMessage());
        }
        return null;
    }

    public Response postForm(String url, Map<String, String> header, String data) {
        Builder builder = new Builder();
        if (header != null) {
            header.forEach(builder::header);
        }
        try {
            RequestBody body;
            String mediaType = "application/x-www-form-urlencoded; charset=utf-8";
            if (data == null) {
                body = RequestBody.create("", MediaType.parse(mediaType));
            } else {
                body = RequestBody.create(data
                    , MediaType.parse(mediaType));
            }
            Response response = client.newCall(builder.post(body).url(url).build()).execute();
            return response;
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
        try {
            RequestBody body;
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            if (data == null) {
                body = RequestBody.create("", mediaType);
            } else {
                body = RequestBody.create(data
                    , mediaType);
            }
            Response response = client.newCall(builder.post(body).url(url).build()).execute();
            return response;
        } catch (IOException e) {
            log.error("请求失败:{}", e.getMessage());
        }
        return null;
    }
}
