package org.start2do.script.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import javax.script.CompiledScript;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.impl.nashornl.ScriptJsNashornImpl;
import org.start2do.script.util.ScriptRunner;
import org.start2do.util.StringUtils;


@Import(NashornJsConfig.class)
public class NashornAutoConfig {

    @Bean(name = "script_js_nashorn")
    @ConditionalOnProperty(prefix = "start2do.script.nashorn", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(IScriptRunner.class)
    public IScriptRunner jsNashorn(ScriptRunnerConfiguration configuration, NashornJsConfig config) {
        Duration duration = null;
        if (config.getExpireAfterAccess() != null) {
            duration = config.getExpireAfterAccess();
        }
        Cache<String, CompiledScript> caffeine;
        if (duration != null) {
            caffeine = Caffeine.newBuilder().expireAfterAccess(duration).build();
        } else {
            caffeine = Caffeine.newBuilder().build();
        }
        if (StringUtils.isEmpty(config.getGlobalScript())) {
            config.setGlobalScript("");
        }
        if (config.isEnableDB()) {
            config.setGlobalScript(config.getGlobalScript()
                + "var DB = Java.type('org.start2do.script.util.impl.functions.DBOperateFunction');");
        }
        if (config.isEnableJsonOperate()) {
            config.setGlobalScript(config.getGlobalScript()
                + "var JSONUtil = Java.type('org.start2do.script.util.impl.functions.JacksonOperateFunction');");
        }
        if (config.isEnableHTTP()) {
            config.setGlobalScript(config.getGlobalScript()
                + "var HTTP = Java.type('org.start2do.script.util.impl.functions.HttpUtil');\r\n");
        }
        IScriptRunner runnerJs = new ScriptJsNashornImpl(
            config.getWhiteList(),
            caffeine,
            config.getGlobalScript(),
            config.getMaxCPUTime(),
            config.getMaxMemory(),
            config.getMaxPoolSize()
        );
        if (configuration.getDefaultRunner() == Type.Nashorn) {
            ScriptRunner.setDefaultInstance(runnerJs);
        } else {
            ScriptRunner.addImpl(runnerJs);
        }
        return runnerJs;
    }
}
