package org.start2do.plugin.client.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 插件客户端配置
 *
 * prefix: plugin.client
 */
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "plugin.client")
public class PluginClientProperties {

    /**
     * 插件管理服务地址，例如：http://plugin-server:8080
     */
    private String serverBaseUrl = "http://localhost:8082";

    /**
     * 当前节点标识，默认使用应用名 + 端口，可由业务手动指定
     */
    private String nodeId;

    /**
     * 当前应用名
     */
    private String appName = "plugin-client-app";

    /**
     * 注册与心跳间隔（秒）
     */
    private long heartbeatIntervalSeconds = 30;

    /**
     * 当前节点对外可访问的 IP
     * <p>
     * 场景：
     * - 在 Docker / K8s 等容器环境下，InetAddress.getLocalHost() 可能返回容器内部 IP，
     *   无法被运维或其他系统直接访问；
     * - 通过该配置可显式指定对外暴露的 IP 地址或主机名。
     * <p>
     * 说明：
     * - 若不配置，则仍回退使用 InetAddress.getLocalHost().getHostAddress()；
     * - nodeId 未显式配置时，会使用 appName@ip 作为默认值。
     */
    private String ip;

    /**
     * 认证令牌
     * <p>
     * 用于与 plugin-server 进行认证，必须与服务端配置的 token 一致
     */
    private String authToken;
}
