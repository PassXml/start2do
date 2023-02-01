package org.start2do.util.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public abstract class AbsOKHttpClient {

    protected final Logger log;


    protected RestTemplate restTemplate;


    public OkHttpClient okHttpConfigClient(ConnectionPool pool, Integer connectTimeout, int readTimeout,
        int writeTimeout) {
        Builder builder = new OkHttpClient().newBuilder().connectionPool(pool)
            .connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS).hostnameVerifier((hostname, session) -> true)
            .addInterceptor(new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT));
        getInterceptors().forEach(builder::addInterceptor);
        return builder.build();
    }

    public List<Interceptor> getInterceptors() {
        return new ArrayList<>();
    }

    public AbsOKHttpClient(String clientName, Integer connectTimeout, int readTimeout, int writeTimeout,
        int maxIdleConnections, int keepAliveDuration) {
        ConnectionPool pool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
        OkHttpClient client = okHttpConfigClient(pool, connectTimeout, readTimeout, writeTimeout);
        OkHttp3ClientHttpRequestFactory httpRequestFactory = new OkHttp3ClientHttpRequestFactory(client);
        this.log = LoggerFactory.getLogger(clientName);
        this.restTemplate = new RestTemplate(httpRequestFactory);
    }

}
