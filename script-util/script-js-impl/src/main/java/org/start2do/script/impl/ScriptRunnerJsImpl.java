package org.start2do.script.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.ScriptJsCache;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.util.Md5Util;
import org.start2do.util.StringUtils;

/**
 * js脚本 必须有main方法而且必须有返回
 */
@Slf4j
public class ScriptRunnerJsImpl implements IScriptRunner<ScriptJsCache> {

    private static ScriptRunnerJsImpl INSTANCE;
    private static Cache<String, ScriptJsCache> SCRIPT_CACHE;
    @Getter
    private static Set<String> WHITE_LIST = new HashSet<>();
    private static Predicate<String> PREDICATE;
    private static String GLOBAL_SCRIPT;
    private static String SYSTEM_GLOBAL_SCRIPT = new StringBuilder().append(
            "var JString = Java.type('java.lang.String');").append("var JInteger = Java.type('java.lang.Integer');")
        .append("var JLong = Java.type('java.lang.Long');").append("var JDouble = Java.type('java.lang.Double');")
        .append("var JBigDecimal = Java.type('java.math.BigDecimal');").toString();

    public ScriptRunnerJsImpl(List<Class<?>> whiteList, Cache<String, ScriptJsCache> caffeine, String globalScript) {
        ScriptRunnerJsImpl.INSTANCE = this;
        ScriptRunnerJsImpl.GLOBAL_SCRIPT = globalScript;
        ScriptRunnerJsImpl.SCRIPT_CACHE = caffeine;
        ScriptRunnerJsImpl.PREDICATE = clazzName -> {
            log.debug("访问,Java Class:{}", clazzName);
            return WHITE_LIST.contains(clazzName);
        };
        initWhiteList(whiteList);
    }

    public ScriptRunnerResult eval(ScriptRunnerInput input) {
        return eval(input.getScript(), input.getParams());
    }


    public ScriptRunnerJsImpl() {
        ScriptRunnerJsImpl.INSTANCE = this;
        ScriptRunnerJsImpl.SCRIPT_CACHE = Caffeine.newBuilder().maximumSize(2000)
            .expireAfterAccess(Duration.ofMinutes(10)).build();
        ScriptRunnerJsImpl.PREDICATE = clazzName -> {
            log.debug("访问,Java Class:{}", clazzName);
            return WHITE_LIST.contains(clazzName);
        };
        initWhiteList(null);
    }

    public ScriptRunnerJsImpl(List<Class<?>> whiteList) {
        ScriptRunnerJsImpl.INSTANCE = this;
        ScriptRunnerJsImpl.SCRIPT_CACHE = Caffeine.newBuilder().maximumSize(2000)
            .expireAfterAccess(Duration.ofMinutes(10)).build();
        ScriptRunnerJsImpl.PREDICATE = clazzName -> {
            log.debug("访问,Java Class:{}", clazzName);
            return WHITE_LIST.contains(clazzName);
        };
        initWhiteList(whiteList);
    }

    public ScriptRunnerJsImpl(List<Class<?>> whiteList, Cache<String, ScriptJsCache> cache, Predicate<String> predicate,
        String globalScript) {
        ScriptRunnerJsImpl.INSTANCE = this;
        ScriptRunnerJsImpl.GLOBAL_SCRIPT = globalScript;
        ScriptRunnerJsImpl.SCRIPT_CACHE = cache;
        ScriptRunnerJsImpl.PREDICATE = predicate;
        initWhiteList(whiteList);
    }

    private void initWhiteList(List<Class<?>> whiteList) {
        for (Class<?> aClass : Arrays.asList(BigDecimal.class, String.class, Long.class, Integer.class, Math.class,
            Short.class, Byte.class, Character.class, Double.class, Float.class, Boolean.class, HashMap.class,
            ArrayList.class)) {
            WHITE_LIST.add(aClass.getName());
        }
        if (whiteList != null) {
            for (Class<?> aClass : whiteList) {
                WHITE_LIST.add(aClass.getName());
            }
        }
    }

    protected ScriptJsCache initScript(String script) {
        ScriptJsCache cache = new ScriptJsCache();
        script = SYSTEM_GLOBAL_SCRIPT + script;
        if (StringUtils.isNotEmpty(GLOBAL_SCRIPT)) {
            script = GLOBAL_SCRIPT + script;
        }
        Context context = Context.newBuilder("js").allowHostAccess(
                HostAccess.newBuilder().allowPublicAccess(true).allowBigIntegerNumberAccess(true).allowMapAccess(true)
                    .allowIterableAccess(true).allowArrayAccess(true).allowListAccess(true).build()).allowIO(IOAccess.NONE)
            .allowHostClassLookup(PREDICATE).logHandler(cache.getConsoleInfo()).err(cache.getConsoleInfo())
            .out(cache.getErrorInfo()).option("engine.WarnInterpreterOnly", "false").timeZone(ZoneId.systemDefault())
            .build();
        Value value = context.eval("js", script);
        if (!value.canExecute()) {
            script += "((args)=>{var result= main(args);if(result){return result;}else{return false;}})";
            value = context.eval("js", script);
        }
        cache.setScript(value);
        return cache;
    }


    @Override
    public ScriptRunnerResult eval(String script, Object... params) {
        String md5 = Md5Util.md5(script);
        return evalMain(md5, script, true, params);
    }

    public ScriptRunnerResult evalMain(String id, String script, boolean isCache, Object... params) {
        ScriptJsCache cache;
        if (isCache) {
            cache = SCRIPT_CACHE.getIfPresent(id);
            if (cache == null) {
                cache = initScript(id, script);
            }
        } else {
            cache = initScript(script);
        }
        try {
            log.debug("脚本:\r\n{}", script);
            Value execute = cache.getScript().execute(params);
            return new ScriptRunnerResult(execute.as(Object.class)).setErrorInfo(cache.getConsoleOutInfo())
                .setConsoleInfo(cache.getErrorOutInfo());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ScriptRunnerResult(null).setSuccess(false).setErrorInfo(e.getMessage());
        }
    }

    @Override
    public ScriptRunnerResult evalById(String id, Object... params) {
        ScriptJsCache cache = SCRIPT_CACHE.getIfPresent(id);
        if (cache == null) {
            return ScriptRunnerResult.fail("脚本不存在");
        }
        return evalMain(id, null, true, params);
    }

    private ScriptJsCache initScript(String md5, String script) {
        ScriptJsCache result = initScript(script);
        SCRIPT_CACHE.put(md5, result);
        return result;
    }

    @Override
    public void clearAllCache() {
        SCRIPT_CACHE.cleanUp();
    }

    @Override
    public boolean hasCacheByScript(String script) {
        return hasCacheById(Md5Util.md5(script));
    }

    @Override
    public boolean hasCacheById(String id) {
        ScriptJsCache present = SCRIPT_CACHE.getIfPresent(id);
        return present != null;
    }

    @Override
    public void removeById(String id) {
        SCRIPT_CACHE.invalidate(id);
    }

    @Override
    public void removeByScript(String script) {
        SCRIPT_CACHE.invalidate(Md5Util.md5(script));
    }

    @Override
    public ScriptRunnerResult evalNoCache(String script, Object[] objects) {
        return evalMain(null, script, false, objects);
    }

    @Override
    public Type getKey() {
        return Type.GraalJS;
    }

    @Override
    public ScriptJsCache preLoad(String script) {
        return initScript(Md5Util.md5(script), script);
    }

    @Override
    public ScriptJsCache preLoad(String id, String script) {
        return initScript(id, script);
    }

}
