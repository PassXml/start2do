package org.start2do;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.start2do.config.ZLMediaKitConfig;
import org.start2do.dto.BaseResp;
import org.start2do.dto.req.AddStreamProxy;
import org.start2do.util.spring.AbsHttpClient;

@Slf4j
@Import(ZLMediaKitConfig.class)
@Component
@ConditionalOnProperty(prefix = "zl-media-kit", value = "enable", havingValue = "true")
@RequiredArgsConstructor
public class ZLMediaKitUtil extends AbsHttpClient {

    private final ZLMediaKitConfig config;
    private final ObjectMapper objectMapper;

    public String getWebSocketFlv(AddStreamProxy req) {
        return config.isSsl() ? "wss://"
            : "ws://" + config.getPullHost() + "/" + req.getApp() + "/" + req.getStream() + ".live.flv?vhost="
                + config.getVHost();
    }

    public void addRtspProxy(AddStreamProxy req) {
        Map<Object, Object> map = Map.of("secret", config.getSecret(), "vhost", config.getVHost(), "app", req.getApp(),
            "stream", req.getStream(), "modify_stamp", "1", "url", req.getUrl(), "enable_rtmp", "1", "enable_ts", "1",
            "enable_fmp4", "1"
        );
        BaseResp resp = restTemplate.getForObject(config.getServerHost() + AddStreamProxy.URL, BaseResp.class, map);
        if (resp.getCode() != 0) {
            throw new RuntimeException(resp.getMsg());
        }
    }

    public void closeRtspProxy(String key) {
        Map<Object, Object> map = Map.of("secret", config.getSecret(), "key", key);
        BaseResp resp = restTemplate.getForObject(
            config.getServerHost() + "/index/api/delStreamProxy", BaseResp.class,
            map
        );
        if (resp.getCode() != 0) {
            throw new RuntimeException(resp.getMsg());
        }
    }

    @Override
    public String getLoggerName() {
        return "ZLMediaKitUtil";
    }
}
