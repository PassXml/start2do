package org.start2do.script.util;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration;
import org.start2do.script.ScriptRunnerConfiguration.JsSetting;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.ScriptJsCache;
import org.start2do.script.util.impl.ScriptRunnerJsImpl;

/**
 * js脚本 必须有main方法而且必须有返回
 */
@Slf4j
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@Import(ScriptRunnerConfiguration.class)
@ConditionalOnProperty(prefix = "start2do.script", name = "enable", havingValue = "true")
public class ScriptRunnerJsAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "start2do.script.js-setting", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(IScriptRunner.class)
    public IScriptRunner js(ScriptRunnerConfiguration configuration) {
        ScriptRunnerJsImpl runnerJs;
        JsSetting jsSetting = configuration.getJsSetting();
        if (jsSetting == null) {
            runnerJs = new ScriptRunnerJsImpl();
        } else {
            Cache<String, ScriptJsCache> caffeine = Caffeine.newBuilder()
                .expireAfterAccess(jsSetting.getExpireAfterAccess()).build();
            runnerJs = new ScriptRunnerJsImpl(jsSetting.getClazzList(), caffeine, jsSetting.getGlobalScript());
        }
        if (configuration.getDefaultRunner() == Type.JS) {
            ScriptRunner.setDefaultInstance(runnerJs);
        } else {
            ScriptRunner.addImpl(runnerJs);
        }
        return runnerJs;
    }
}
