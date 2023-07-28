package org.start2do.util.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.NoArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@NoArgsConstructor
public abstract class AbsOKHttpClient {

    protected Logger log;


    protected RestTemplate restTemplate;
    protected OkHttpClient okHttpClient;

    public MediaType wwwFromMediaType() {
        return MediaType.parse("application/x-www-form-urlencoded");
    }

    public MediaType jsonUTF8() {
        return MediaType.parse("application/json");
    }

    public RequestBody jsonUTF8(String jsonSrt) {
        return RequestBody.create(jsonSrt, jsonUTF8());
    }


    public CookieJar getCookieJar() {
        return CookieJar.NO_COOKIES;
    }

    public OkHttpClient okHttpConfigClient(ConnectionPool pool, Integer connectTimeout, int readTimeout,
        int writeTimeout) {
        Builder builder = new OkHttpClient().newBuilder().connectionPool(pool)
            .cookieJar(getCookieJar())
            .connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS).hostnameVerifier((hostname, session) -> true)
            .addInterceptor(new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT));
        getInterceptors().forEach(builder::addInterceptor);
        return builder.build();
    }

    public void setClientConfig(Builder config) {

    }

    public List<Interceptor> getInterceptors() {
        return new ArrayList<>();
    }

    public void build(String clientName, Integer connectTimeout, int readTimeout, int writeTimeout,
        int maxIdleConnections, int keepAliveDuration) {
        ConnectionPool pool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
        okHttpClient = okHttpConfigClient(pool, connectTimeout, readTimeout, writeTimeout);
        OkHttp3ClientHttpRequestFactory httpRequestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
        this.log = LoggerFactory.getLogger(clientName);
        this.restTemplate = new RestTemplate(httpRequestFactory);
    }

    public static class CookieJarManager implements CookieJar {


        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            if (null == url || null == cookies || cookies.size() <= 0) {
                return;
            }
            cookieStore.put(url.host(), cookies);

        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            if (null != url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            } else {
                return new ArrayList<>();
            }
        }
    }

}
