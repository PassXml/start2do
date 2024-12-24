package org.start2do.script.util.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.EvalMode;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.start2do.script.IScriptRunner;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.util.impl.av_function.ConsoleFunction;
import org.start2do.script.util.impl.av_function.PutFunction;
import org.start2do.util.Md5Util;

/**
 * js脚本 必须有main方法而且必须有返回
 */
@Slf4j
public class ScriptRunnerAvImpl implements IScriptRunner {

    private static Cache<String, Expression> SCRIPT_CACHE;
    private static AviatorEvaluatorInstance INSTANCE;

    private void inti() {
        INSTANCE = AviatorEvaluator.newInstance(EvalMode.ASM)
            .useLRUExpressionCache(5000);
        INSTANCE.setOption(Options.MAX_LOOP_COUNT, 100);
        INSTANCE.setOption(Options.EVAL_TIMEOUT_MS, Duration.ofMinutes(5).toMillis());
        INSTANCE.addFunction(new PutFunction());
        INSTANCE.addFunction(new ConsoleFunction());
    }

    public ScriptRunnerAvImpl() {
        inti();
        ScriptRunnerAvImpl.SCRIPT_CACHE = Caffeine.newBuilder().maximumSize(2000)
            .expireAfterAccess(Duration.ofMinutes(10)).build();
    }

    public ScriptRunnerAvImpl(List<Class<? extends AbstractFunction>> functions) {
        inti();
        ScriptRunnerAvImpl.SCRIPT_CACHE = Caffeine.newBuilder().maximumSize(2000)
            .expireAfterAccess(Duration.ofMinutes(10)).build();
        for (Class<? extends AbstractFunction> aClass : functions) {
            try {
                Constructor<? extends AbstractFunction> constructor = aClass.getDeclaredConstructor();
                AbstractFunction instance = constructor.newInstance();
                INSTANCE.addFunction(instance);
            } catch (Exception e) {
                log.error("初始化函数失败,{},{}", aClass.getName(), e.getMessage());
            }
        }
    }

    @Override
    public ScriptRunnerResult eval(ScriptRunnerInput input) {
        return eval(input.getScript(), input.getParams());
    }

    @Override
    public ScriptRunnerResult eval(String script, Object... params) {
        INSTANCE.validate(script);
        String md5 = Md5Util.md5(script);
        Expression cache = SCRIPT_CACHE.getIfPresent(md5);
        if (cache == null) {
            cache = INSTANCE.compile(script, true);
            SCRIPT_CACHE.put(md5, cache);
        }
        try {
            Object execute = cache.execute(cache.newEnv(params));
            return new ScriptRunnerResult(execute);
        } catch (Exception e) {
            log.error("脚本执行失败", e);
            return new ScriptRunnerResult().setSuccess(false).setErrorInfo(e.getMessage());
        }
    }
}
