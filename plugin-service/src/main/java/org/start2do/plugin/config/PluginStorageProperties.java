package org.start2do.plugin.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 插件存储相关配置
 * <p>
 * prefix: plugin 例如： plugin.storage-path=./plugins/eip
 */
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "plugin")
public class PluginStorageProperties {

    /**
     * 插件存放目录 默认为当前工作目录下的 ./plugins/eip
     */
    private String storagePath = "./plugins/eip";
}

