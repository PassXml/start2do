package org.start2do;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "start2do.business.init")
public class BusinessSettingInitConfiguration {

    private List<SettingItem> settings;

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class SettingItem {

        /**
         * 配置 key
         */
        private String key;

        /**
         * 配置值
         */
        private String value;

        /**
         * 是否启用：yes=1, no=0
         */
        private Boolean enable;

        /**
         * 排序值
         */
        private Integer sort;
        private String remark;
        private String type = "SYS";

    }
}
