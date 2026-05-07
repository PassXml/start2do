package org.start2do.script.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.EvalMode;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.impl.av_function.ConsoleFunction;
import org.start2do.script.impl.av_function.PutFunction;
import org.start2do.util.Md5Util;

/**
 * js脚本 必须有main方法而且必须有返回
 */
@Slf4j
public class ScriptRunnerAvImpl implements IScriptRunner<Expression> {

    private Cache<String, Expression> SCRIPT_CACHE;
    @Getter
    private AviatorEvaluatorInstance INSTANCE;

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
        SCRIPT_CACHE = Caffeine.newBuilder().maximumSize(2000).build();
    }

    public ScriptRunnerAvImpl(List<Class<? extends AbstractFunction>> functions) {
        inti();
        SCRIPT_CACHE = Caffeine.newBuilder().maximumSize(2000).build();
        if (functions != null) {
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
    }

    @Override
    public ScriptRunnerResult eval(ScriptRunnerInput input) {
        return eval(input.getScript(), input.getParams());
    }

    @Override
    public ScriptRunnerResult eval(String script, Object... params) {
        String md5 = Md5Util.md5(script);
        return evalMain(md5, script, true, params);
    }

    public ScriptRunnerResult evalMain(String id, String script, boolean isCache, Object... params) {
        Expression cache;
        if (isCache) {
            cache = SCRIPT_CACHE.getIfPresent(id);
            if (cache == null) {
                cache = preLoad(id, script);
            }
        } else {
            cache = INSTANCE.compile(script, false);
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Map<String, Object> env = cache.newEnv(params);
            env.put(ConsoleFunction.CONSOLEKEY, outputStream);
            Object execute = cache.execute(env);
            ScriptRunnerResult result = new ScriptRunnerResult(execute);
            result.setConsoleInfo(outputStream.toString());
            return result;
        } catch (Exception e) {
            log.error("脚本执行失败", e);
            return new ScriptRunnerResult().setSuccess(false).setErrorInfo(e.getMessage());
        }

    }

    @Override
    public ScriptRunnerResult evalById(String id, Object... params) {
        return evalMain(id, null, true, params);
    }

    @Override
    public void clearAllCache() {
        INSTANCE.clearExpressionCache();
        SCRIPT_CACHE.invalidateAll();
    }

    @Override
    public boolean hasCacheByScript(String script) {
        return hasCacheById(Md5Util.md5(script));
    }

    @Override
    public boolean hasCacheById(String id) {
        Expression expression = SCRIPT_CACHE.getIfPresent(id);
        return expression != null;
    }

    @Override
    public void removeById(String id) {
        SCRIPT_CACHE.invalidate(id);
        INSTANCE.invalidateCacheByKey(id);
    }

    @Override
    public void removeByScript(String script) {
        removeById(Md5Util.md5(script));
    }

    @Override
    public ScriptRunnerResult evalNoCache(String scprit, Object[] objects) {
        return evalMain(null, scprit, false, objects);
    }

    @Override
    public Type getKey() {
        return Type.Aviator;
    }

    @Override
    public Expression preLoad(String script) {
        String md5 = Md5Util.md5(script);
        return preLoad(md5, script);
    }

    public Expression preLoad(String md5, String script) {
        INSTANCE.validate(script);
        Expression cache = INSTANCE.compile(md5, script, true);
        SCRIPT_CACHE.put(md5, cache);
        return cache;
    }

    @Override
    public void addFunction(Class<?>... functionClasses) {
        if (functionClasses == null || functionClasses.length == 0) {
            return;
        }
        for (Class<?> functionClass : functionClasses) {
            if (functionClass == null || !AbstractFunction.class.isAssignableFrom(functionClass)) {
                continue;
            }
            try {
                Constructor<?> constructor = functionClass.getDeclaredConstructor();
                AbstractFunction instance = (AbstractFunction) constructor.newInstance();
                INSTANCE.addFunction(instance);
            } catch (Exception e) {
                log.error("动态注册函数失败,{},{}", functionClass.getName(), e.getMessage());
            }
        }
    }

}
