package org.start2do;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.start2do.config.ZLMediaKitConfig;
import org.start2do.dto.BaseResp;
import org.start2do.dto.req.AddStreamProxy;
import org.start2do.util.spring.AbsOKHttpClient;

@Slf4j
@Import(ZLMediaKitConfig.class)
@ConditionalOnProperty(prefix = "zl-media-kit", name = "enable", havingValue = "true")
@RequiredArgsConstructor
public class ZLMediaKitUtil extends AbsOKHttpClient {

    private final ZLMediaKitConfig config;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        build("ZLMediaKitUtil", 3000, 3000, 3000, 5, 5);
    }

    public String getWebSocketFlv(AddStreamProxy req) {
        return config.isSSL() ? "wss://"
            : "ws://" + config.getIp() + "/" + req.getApp() + "/" + req.getStream()
                + ".live.flv?vhost=" + config.getVHost();
    }

    public void addRtspProxy(AddStreamProxy req) {
        try {
            HttpUrl url = HttpUrl.get(config.getHost() + AddStreamProxy.URL);
            HttpUrl httpUrl = url.newBuilder().addQueryParameter("secret", config.getSecret())
                .addQueryParameter("vhost", config.getVHost()).addQueryParameter("app", req.getApp())
                .addQueryParameter("stream", req.getStream()).addQueryParameter("url", req.getUrl())
                .addQueryParameter("modify_stamp", "1").addQueryParameter("url", req.getUrl())
                .addQueryParameter("enable_rtmp", "1").addQueryParameter("enable_ts", "1")
                .addQueryParameter("enable_fmp4", "1").build();
            Response response = okHttpClient.newCall(new Builder().url(httpUrl).get().build()).execute();
            BaseResp resp = objectMapper.readValue(response.body().string(), BaseResp.class);
            if (resp.getCode() != 0) {
                throw new RuntimeException(resp.getMsg());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
