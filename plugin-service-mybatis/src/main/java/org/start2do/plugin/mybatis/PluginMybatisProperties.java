package org.start2do.plugin.mybatis;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 插件 MyBatis 相关配置属性
 * <p>
 * 用于宿主侧声明可供插件复用的逻辑数据源 ID 与对应的 SqlSessionTemplate Bean 名称。
 */
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "plugin.mybatis")
public class PluginMybatisProperties {

    /**
     * 宿主侧暴露给插件使用的 SqlSessionTemplate 映射列表
     */
    private List<HostDatasourceConfig> hostDatasources;

    @Getter
    @Setter
    @ToString
    public static class HostDatasourceConfig {

        /**
         * 逻辑数据源 ID，例如 primary、report 等
         */
        private String id;

        /**
         * 宿主中已经配置好的 SqlSessionTemplate Bean 名称
         */
        private String sqlSessionTemplateBean;
    }
}

