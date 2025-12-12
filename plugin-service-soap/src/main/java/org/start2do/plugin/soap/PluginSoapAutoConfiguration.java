package org.start2do.plugin.soap;

import org.apache.cxf.Bus;
import org.pf4j.PluginManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 插件 SOAP 自动配置
 * <p>
 * 条件：
 * 1. 类路径中存在 CXF 的 Bus
 * 2. 容器中存在 PluginManager（说明 PF4J 已启用）
 *
 * 注意：使用 {@link AutoConfigureAfter} 的 name 形式，避免对 plugin-service 产生编译期依赖。
 */
@Configuration
@ConditionalOnClass(Bus.class)
@ConditionalOnBean({PluginManager.class, Bus.class})
@AutoConfigureAfter(name = "org.start2do.plugin.config.PluginAutoPluginConfiguration")
@Import(Pf4jSoapBridge.class)
public class PluginSoapAutoConfiguration {
    // 空类，仅用于触发 Pf4jSoapBridge 的装配
}

