package org.start2do.plugin.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 插件系统统一配置
 * <p>
 * prefix: plugin
 *
 * 示例：
 * plugin:
 *   runtime:
 *     storage-path: ./plugins/eip
 *   server:
 *     storage-path: ./plugins/server
 */
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "plugin")
public class PluginSystemProperties {

    /**
     * 运行时配置（业务节点本地 PF4J 插件目录）
     */
    private Runtime runtime = new Runtime();

    /**
     * 管理端配置（插件管理服务的仓库与元数据目录）
     */
    private Server server = new Server();

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Runtime {

        /**
         * 节点本地 PF4J 加载插件的目录
         * 例如： ./plugins/eip
         * 生效模块：
         * - plugin-service：插件启用/停用/删除/加载
         * - plugin-client：从管理端下载 JAR 后写入该目录
         */
        private String storagePath = "./plugins/eip";
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Server {

        /**
         * 管理端插件仓库与元数据根目录
         * 例如： ./plugins/server
         * 在该目录下按约定创建：
         * - repository/：插件版本仓库
         * - releases/：发布与灰度配置
         * - cluster/nodes/：节点注册与心跳信息
         */
        private String storagePath = "./plugins/server";

        /**
         * 认证配置
         */
        private Auth auth = new Auth();
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Auth {

        /**
         * 是否启用认证
         */
        private boolean enabled = false;

        /**
         * 认证令牌
         */
        private String token;
    }
}

