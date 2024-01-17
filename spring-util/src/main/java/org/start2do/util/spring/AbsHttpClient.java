package org.start2do.util.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public abstract class AbsHttpClient implements ClientHttpRequestInterceptor {

    protected final Logger log;

    public abstract String getLoggerName();

    protected RestTemplate restTemplate;

    public AbsHttpClient() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.of(30, ChronoUnit.SECONDS)).build());
        BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(
            factory);
        this.restTemplate = new RestTemplate(bufferingClientHttpRequestFactory);
        this.restTemplate.getInterceptors().add(this);
        this.log = LoggerFactory.getLogger(getLoggerName());
    }

    protected static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[4096];
            int n;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }
            byte[] var4 = output.toByteArray();
            byte[] var6 = var4;
            return var6;
        } finally {
            output.close();
        }
    }

    protected String headerToString(HttpHeaders headers) {
        StringBuilder stringJoiner = (new StringBuilder()).append("{");
        headers.forEach((s, strings) -> {
            stringJoiner.append(s).append(":[").append(String.join(",", strings)).append("]");
        });
        stringJoiner.append("}");
        return stringJoiner.toString();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        log.info("URL:{} {} ,Header: {} Request:{} ,ResponseHeader:{} ,ResponseStatus:{} ,ResponseBody:{}",
            request.getURI(), request.getMethod().name(), this.headerToString(request.getHeaders()),
            new String(body, "UTF-8"), this.headerToString(response.getHeaders()), response.getStatusCode(),
            new String(toByteArray(response.getBody()), "UTF-8"));
        return response;
    }

    protected HttpHeaders getHeader(MediaType type) {
        HttpHeaders result = new HttpHeaders();
        if (type == null) {
            type = MediaType.APPLICATION_JSON;
        }
        result.add("content-type", type.toString());
        return result;
    }

    protected <T> HttpEntity<T> getHttpEntity(T req, MediaType type) {
        return new HttpEntity<T>(req, getHeader(type));
    }

}
