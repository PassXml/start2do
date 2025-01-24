package org.start2do.script.util;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.FunctionMissing;
import com.googlecode.aviator.runtime.JavaMethodReflectionFunctionMissing;
import java.lang.reflect.Constructor;
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
import org.start2do.script.ScriptRunnerConfiguration.AvSetting;
import org.start2do.script.ScriptRunnerConfiguration.JsSetting;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.ScriptJsCache;
import org.start2do.script.util.impl.ScriptRunnerAvImpl;
import org.start2do.script.util.impl.ScriptRunnerJsImpl;
import org.start2do.script.util.impl.functions.DBOperateFunction;
import org.start2do.script.util.impl.functions.HttpUtil;
import org.start2do.script.util.impl.functions.JacksonOperateFunction;

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
public class ScriptRunnerAutoConfiguration {

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

    @Bean
    @ConditionalOnProperty(prefix = "start2do.script.av-setting", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(IScriptRunner.class)
    public IScriptRunner aviator(ScriptRunnerConfiguration configuration) {
        AvSetting avSetting = configuration.getAvSetting();
        ScriptRunnerAvImpl runnerAv;
        if (avSetting == null) {
            runnerAv = new ScriptRunnerAvImpl();
        } else {
            runnerAv = new ScriptRunnerAvImpl(avSetting.getFunctions());
            if (Boolean.TRUE.equals(avSetting.getEnableJacksonFunction())) {
                addFunction(runnerAv.getINSTANCE(), "JSON", JacksonOperateFunction.class);
            }
            if (Boolean.TRUE.equals(avSetting.getEnableHikariDataSource())) {
                addFunction(runnerAv.getINSTANCE(), "DB", DBOperateFunction.class);
            }
            if (Boolean.TRUE.equals(avSetting.getEnableOkhttpClient())) {
                addFunction(runnerAv.getINSTANCE(), "HTTP", HttpUtil.class);
            }
            if (Boolean.TRUE.equals(avSetting.getEnableSystemFunctionMissing())) {
                runnerAv.getINSTANCE().setFunctionMissing(JavaMethodReflectionFunctionMissing.getInstance());
            } else {
                Class<? extends FunctionMissing> aClass = avSetting.getCustomFunctionMissingImpl();
                if (aClass != null) {
                    Constructor<?>[] constructors = aClass.getDeclaredConstructors();
                    try {
                        Object instance = constructors[0].newInstance();
                        runnerAv.getINSTANCE().setFunctionMissing((FunctionMissing) instance);
                    } catch (Exception e) {
                        log.error("初始化错误:{}", e.getMessage());
                    }
                }
            }
            if (avSetting.getImportStaticFunction() != null) {
                for (Class<?> aClass : avSetting.getImportStaticFunction()) {
                    try {
                        runnerAv.getINSTANCE().importFunctions(aClass);
                    } catch (Exception e) {
                        log.error("import Functions error,{}", e.getMessage());
                    }
                }
            }
        }
        if (configuration.getDefaultRunner() == Type.Aviator) {
            ScriptRunner.setDefaultInstance(runnerAv);
        } else {
            ScriptRunner.addImpl(runnerAv);
        }
        return runnerAv;
    }

    private void addFunction(AviatorEvaluatorInstance instance, String ns, Class aClass) {
        try {
            instance.addStaticFunctions(ns, aClass);
        } catch (Exception e) {
            log.error("importFunctions error,{}", e.getMessage());
        }
    }
}
