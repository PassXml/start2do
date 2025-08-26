package org.start2do;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.start2do.constant.ErrorConstant;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "start2do")
public class SpringCommonConfig {


    private boolean enableException = true;
    private ReplaceFilter replaceFilter;
    private ErrorTrace errorTrace;
    private Map<String, String> errorMsgs = new HashMap<>(32) {
        {
            put(ErrorConstant.AUTHENTICATION_FAILED_OR_EXPIRED, "认证失败或者凭证过期");
            put(ErrorConstant.PERMISSION_DENIED, "权限不足");
            put(ErrorConstant.NOT_LOGIN, "未登录");
            put(ErrorConstant.NO_PERMISSION, "无权限");
        }
    };


    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class ReplaceFilter {

        private Boolean enable;
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class ErrorTrace {

        private String packageName;
    }

}
