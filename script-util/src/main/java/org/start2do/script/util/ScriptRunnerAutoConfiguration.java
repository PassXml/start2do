package org.start2do.script.util;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration;
import org.start2do.script.ScriptRunnerConfiguration.AvSetting;
import org.start2do.script.ScriptRunnerConfiguration.JsSetting;
import org.start2do.script.dto.ScriptJsCache;
import org.start2do.script.util.impl.ScriptRunnerAvImpl;
import org.start2do.script.util.impl.ScriptRunnerJsImpl;

/**
 * js脚本 必须有main方法而且必须有返回
 */
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Configuration
@Import(ScriptRunnerConfiguration.class)
@ConditionalOnProperty(prefix = "start2do.script", name = "enable", havingValue = "true")
public class ScriptRunnerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "start2do.script", name = "type", havingValue = "js")
    @ConditionalOnMissingBean(IScriptRunner.class)
    public IScriptRunner js(ScriptRunnerConfiguration configuration) {
        ScriptRunnerJsImpl runnerJs;
        JsSetting jsSetting = configuration.getJsSetting();
        if (jsSetting == null) {
            runnerJs = new ScriptRunnerJsImpl();
        } else {
            Cache<String, ScriptJsCache> caffeine = Caffeine.newBuilder().expireAfterAccess(
                jsSetting.getExpireAfterAccess()
            ).build();
            runnerJs = new ScriptRunnerJsImpl(
                jsSetting.getClazzList(), caffeine, jsSetting.getGlobalScript()
            );
        }
        ScriptRunner.setINSTANCE(runnerJs);
        return runnerJs;
    }

    @Bean
    @ConditionalOnProperty(prefix = "start2do.script", name = "type", havingValue = "aviator")
    @ConditionalOnMissingBean(IScriptRunner.class)
    public IScriptRunner aviator(ScriptRunnerConfiguration configuration) {
        AvSetting avSetting = configuration.getAvSetting();
        ScriptRunnerAvImpl scriptRunnerAv;
        if (avSetting == null) {
            scriptRunnerAv = new ScriptRunnerAvImpl();
        } else {
            scriptRunnerAv = new ScriptRunnerAvImpl(avSetting.getFunctions());
        }
        ScriptRunner.setINSTANCE(scriptRunnerAv);
        return scriptRunnerAv;
    }
}
