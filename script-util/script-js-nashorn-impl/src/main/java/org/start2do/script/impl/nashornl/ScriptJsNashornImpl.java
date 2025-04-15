package org.start2do.script.impl.nashornl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.BindingDto;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.util.impl.functions.DBOperateFunction;
import org.start2do.script.util.impl.functions.HttpUtil;
import org.start2do.script.util.impl.functions.JacksonOperateFunction;
import org.start2do.util.Md5Util;
import org.start2do.util.StringUtils;

@Slf4j
public class ScriptJsNashornImpl implements IScriptRunner<CompiledScript> {

    @Getter
    private static ScriptJsNashornImpl INSTANCE;
    @Getter
    private ScriptEngine engine;
    private Cache<String, CompiledScript> SCRIPT_CACHE;
    private long maxCPUTime;
    private long maxMemory;
    private String GLOBAL_SCRIPT;
    // 添加 Bindings 池相关字段
    private final Deque<BindingDto> bindingsPool = new ArrayDeque<>();
    private final Lock poolLock = new ReentrantLock();
    // 设置池的最大大小
    private int MAX_POOL_SIZE;
    private ExecutorService executorService;

    // 添加获取 Bindings 的方法
    private BindingDto getBindings() {
        poolLock.lock();
        try {
            BindingDto bindings = bindingsPool.pollFirst();
            if (bindings == null) {
                bindings = apply(engine.createBindings());
            }
            return bindings;
        } finally {
            poolLock.unlock();
        }
    }

    // 添加归还 Bindings 的方法
    private void returnBindings(BindingDto bindings) {
        if (bindings == null) {
            return;
        }
        bindings.getBindings().clear();
        bindings.getOutputStream().reset();
        poolLock.lock();
        try {
            if (bindingsPool.size() < MAX_POOL_SIZE) {
                bindingsPool.offerFirst(remove(bindings));
            }
        } finally {
            poolLock.unlock();
        }
    }

    public ScriptJsNashornImpl(Set<String> whiteList) {
        maxMemory = 100 * 1024 * 1024;
        maxCPUTime = 60 * 1000 * 3;
        this.MAX_POOL_SIZE = 20;
        init(whiteList, Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build(), null);
    }

    public ScriptJsNashornImpl(Set<Class<?>> clazzList, Cache<String, CompiledScript> caffeine, String globalScript,
        long maxCPUTime, long maxMemory, int maxPoolSize) {
        this.MAX_POOL_SIZE = maxPoolSize;
        ScriptJsNashornImpl.INSTANCE = this;
        if (clazzList == null) {
            clazzList = new HashSet<>();
        }
        this.maxCPUTime = maxCPUTime;
        this.maxMemory = maxMemory;
        init(clazzList.stream().map(Class::getName).collect(Collectors.toSet()), caffeine, globalScript);

    }

    private BindingDto apply(Bindings bindings) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SystemConsole console = new SystemConsole(outputStream);
        bindings.put("console", console);
        return new BindingDto(
            bindings,
            outputStream,
            new SystemConsole(outputStream)
        );
    }

    private BindingDto remove(BindingDto dto) {
        Bindings bindings = dto.getBindings();
        dto.getOutputStream().reset();
        bindings.remove("quit");
        bindings.remove("exit");
        bindings.remove("load");
        bindings.remove("print");
        bindings.remove("__noSuchProperty__");
        bindings.remove("engine");
        bindings.remove("context");
        bindings.put("console", dto.getSystemConsole());
        return dto;
    }

    private void init(Set<String> set, Cache<String, CompiledScript> caffeine, String globalScript) {
        ScriptJsNashornImpl.INSTANCE = this;
        SCRIPT_CACHE = caffeine;
        executorService = new ThreadPoolExecutor(2, MAX_POOL_SIZE, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());
        set.add("okhttp3.OkHttpClient.Builder");
        set.add("org.start2do.script.util.impl.functions.HttpUtil");
        set.add("org.start2do.script.util.impl.functions.JacksonOperateFunction");
        set.add("org.start2do.script.util.impl.functions.DBOperateFunction");
        set.add("org.openjdk.nashorn.api.linker.NashornLinkerExporter");
        set.add("org.start2do.script.util.impl.functions.CustomConsole");
        this.engine = new NashornScriptEngineFactory().getScriptEngine(new String[]{"--language=es6"},
            new RestrictedClassLoader(set));
        // 初始化 Bindings 池
        poolLock.lock();
        try {
            for (int i = 0; i < MAX_POOL_SIZE; i++) {
                bindingsPool.offerFirst(apply(engine.createBindings()));
            }
        } finally {
            poolLock.unlock();
        }
        if (StringUtils.isNotEmpty(globalScript)) {
            this.GLOBAL_SCRIPT = globalScript;
        }
    }

    public ScriptJsNashornImpl() {
        HashSet<String> whiteList = new HashSet<>();
        whiteList.add(HttpUtil.class.getName());
        whiteList.add(DBOperateFunction.class.getName());
        whiteList.add(JacksonOperateFunction.class.getName());
        whiteList.add(String.class.getName());
        whiteList.add(Integer.class.getName());
        whiteList.add(Long.class.getName());
        whiteList.add(Double.class.getName());
        whiteList.add(BigDecimal.class.getName());
        maxMemory = 100 * 1024 * 1024;
        maxCPUTime = 60 * 1000 * 3;
        this.MAX_POOL_SIZE = 20;
        init(whiteList, Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build(), null);
    }

    @Override
    public ScriptRunnerResult eval(ScriptRunnerInput input) {
        return evalMain(input.getId(), input.getScript(), true, input.getParams());
    }

    @Override
    public ScriptRunnerResult eval(String script, Object... params) {
        return evalMain(null, script, true, params);
    }

    @Override
    public ScriptRunnerResult evalById(String id, Object... params) {
        return evalMain(id, null, true, params);
    }

    @Override
    public CompiledScript preLoad(String script) {
        return preLoad(Md5Util.md5(script), script);
    }

    @Override
    public CompiledScript preLoad(String id, String script) {
        try {
            return ((Compilable) engine).compile(
                StringUtils.isNotEmpty(GLOBAL_SCRIPT) ? GLOBAL_SCRIPT + script : script);
        } catch (ScriptException e) {
            log.error("脚本加载失败,{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearAllCache() {
        SCRIPT_CACHE.invalidateAll();
    }

    @Override
    public boolean hasCacheByScript(String script) {
        return hasCacheById(Md5Util.md5(script));
    }

    @Override
    public boolean hasCacheById(String id) {
        return SCRIPT_CACHE.getIfPresent(id) != null;
    }

    @Override
    public void removeById(String id) {
        SCRIPT_CACHE.invalidate(id);
    }

    @Override
    public void removeByScript(String script) {
        SCRIPT_CACHE.invalidate(Md5Util.md5(script));
    }

    public ScriptRunnerResult evalMain(String id, String script, boolean isCache, Object... params) {
        log.debug("脚本:\r\n{}", script);
        BindingDto bindings = null;
        try {
            Map map = objectsToMap(params);
            bindings = getBindings();
            bindings.getBindings().putAll(map);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CompiledScript compiledScript = null;
            if (isCache) {
                if (StringUtils.isEmpty(id) && StringUtils.isEmpty(script)) {
                    return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本为空或id为空");
                }
                if (StringUtils.isNotEmpty(id)) {
                    compiledScript = SCRIPT_CACHE.getIfPresent(id);
                } else if (StringUtils.isNotEmpty(script)) {
                    String key = Md5Util.md5(script);
                    compiledScript = SCRIPT_CACHE.get(key, s -> {
                        CompiledScript cs = preLoad(script);
                        // 如果编译失败，从缓存中移除
                        if (cs == null) {
                            SCRIPT_CACHE.invalidate(key);
                        }
                        return cs;
                    });
                } else {
                    return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本为空");
                }
            }
            SandboxThread sandboxThread = new SandboxThread(getEngine(), compiledScript, script, bindings.getBindings(),
                maxCPUTime,
                maxMemory);
            executorService.execute(sandboxThread);
            sandboxThread.monitoring();
            if (sandboxThread.isCpuTimeExceeded()) {
                return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本执行超时（" + maxCPUTime + "ms）");
            } else if (sandboxThread.isMemoryExceeded()) {
                return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本内存溢出" + maxCPUTime + "）KB");
            }
            if (Objects.nonNull(sandboxThread.getException())) {
                return new ScriptRunnerResult().setSuccess(false)
                    .setErrorInfo(sandboxThread.getException().getMessage());
            }
            return new ScriptRunnerResult(sandboxThread.getResult()).setConsoleInfo(
                new String(bindings.getOutputStream().toByteArray())
            );
        } catch (Exception e) {
            log.error("脚本执行失败,{},{}", id, script, e);
            return new ScriptRunnerResult().setSuccess(false).setErrorInfo(e.getMessage());
        } finally {
            returnBindings(bindings);
        }
    }

    @Override
    public ScriptRunnerResult evalNoCache(String scirpt, Object[] objects) {
        return evalMain(Md5Util.md5(scirpt), scirpt, false, objects);
    }

    @Override
    public Type getKey() {
        return Type.Nashorn;
    }

    @Slf4j
    public static class RestrictedClassLoader extends ClassLoader {

        @Getter
        private Set<String> WHITE_LIST;

        public RestrictedClassLoader(Set<String> whiteList) {
            this.WHITE_LIST = whiteList;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!WHITE_LIST.contains(name)) {
                log.warn("不允许加载的类:{}", name);
//                throw new ClassNotFoundException("不允许加载的类" + name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
