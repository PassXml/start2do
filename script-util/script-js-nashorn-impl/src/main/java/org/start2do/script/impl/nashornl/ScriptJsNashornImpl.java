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
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import lombok.Getter;
import lombok.Setter;
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
                log.debug("创建新的Bindings实例，当前池大小:{}", bindingsPool.size());
            } else {
                log.debug("从池中获取Bindings实例，当前池大小:{}", bindingsPool.size());
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
        try {
            bindings.getBindings().clear();
            bindings.getOutputStream().reset();
            poolLock.lock();
            try {
                if (bindingsPool.size() < MAX_POOL_SIZE) {
                    bindingsPool.offerFirst(remove(bindings));
                    log.debug("归还Bindings到池中，当前池大小:{}", bindingsPool.size());
                } else {
                    log.debug("Bindings池已满，丢弃Bindings实例");
                }
            } finally {
                poolLock.unlock();
            }
        } catch (Exception e) {
            log.warn("归还Bindings时发生异常:{}", e.getMessage(), e);
        }
    }

    public ScriptJsNashornImpl(Set<String> whiteList, Set<String> blackList) {
        maxMemory = 100 * 1024 * 1024;
        maxCPUTime = 60 * 1000 * 3;
        this.MAX_POOL_SIZE = 20;
        init(whiteList, blackList, Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build(), null);
    }

    public ScriptJsNashornImpl(Set<String> clazzList, Set<String> blackList, Cache<String, CompiledScript> caffeine,
        String globalScript, long maxCPUTime, long maxMemory, int maxPoolSize) {
        this.MAX_POOL_SIZE = maxPoolSize;
        ScriptJsNashornImpl.INSTANCE = this;
        if (clazzList == null) {
            clazzList = new HashSet<>();
        }
        this.maxCPUTime = maxCPUTime;
        this.maxMemory = maxMemory;
        init(clazzList, blackList, caffeine, globalScript);

    }

    private BindingDto apply(Bindings bindings) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SystemConsole console = new SystemConsole(outputStream);
        bindings.put("console", console);
        return new BindingDto(bindings, outputStream, new SystemConsole(outputStream));
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

    private void init(Set<String> whiteList, Set<String> blackList, Cache<String, CompiledScript> caffeine,
        String globalScript) {
        ScriptJsNashornImpl.INSTANCE = this;
        SCRIPT_CACHE = caffeine;
        executorService = new ThreadPoolExecutor(Math.min(4, MAX_POOL_SIZE / 2), MAX_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
        whiteList.add("okhttp3.OkHttpClient.Builder");
        whiteList.add("org.start2do.script.util.impl.functions.HttpUtil");
        whiteList.add("org.start2do.script.util.impl.functions.JacksonOperateFunction");
        whiteList.add("org.start2do.script.util.impl.functions.DBOperateFunction");
        whiteList.add("org.openjdk.nashorn.api.linker.NashornLinkerExporter");
        whiteList.add("org.start2do.script.util.impl.functions.CustomConsole");
        this.engine = new NashornScriptEngineFactory().getScriptEngine(new String[]{"--language=es6"},
            new RestrictedClassLoader(whiteList, blackList));
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
        HashSet<String> blackList = new HashSet<>();
        blackList.add("java.lang.Runtime");
        blackList.add("java.lang.Process");
        blackList.add("java.lang.System");
        blackList.add("java.io.*");
        blackList.add("java.nio.*");
        blackList.add("sun.misc.Unsafe");
        init(whiteList, blackList, Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build(), null);
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
            CompiledScript compiledScript = ((Compilable) engine).compile(
                StringUtils.isNotEmpty(GLOBAL_SCRIPT) ? GLOBAL_SCRIPT + script : script);
            log.debug("脚本加载成功,ID:{}", id);
            return compiledScript;
        } catch (ScriptException e) {
            log.error("脚本加载失败,ID:{},错误信息:{}", id, e.getMessage(), e);
            throw new RuntimeException("脚本加载失败: " + e.getMessage(), e);
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
        log.debug("开始执行脚本,ID:{},脚本内容:\r\n{}", id, script);
        BindingDto bindings = null;
        try {
            Map map = objectsToMap(params);
            bindings = getBindings();
            bindings.getBindings().putAll(map);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CompiledScript compiledScript = null;
            if (isCache) {
                if (StringUtils.isEmpty(id) && StringUtils.isEmpty(script)) {
                    log.warn("脚本执行参数错误:脚本为空或id为空");
                    return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本为空或id为空");
                }
                if (StringUtils.isNotEmpty(id)) {
                    compiledScript = SCRIPT_CACHE.getIfPresent(id);
                    log.debug("从缓存中获取编译脚本,ID:{}", id);
                } else if (StringUtils.isNotEmpty(script)) {
                    String key = Md5Util.md5(script);
                    compiledScript = SCRIPT_CACHE.get(key, s -> {
                        log.debug("缓存中不存在脚本，开始编译,ID:{}", key);
                        CompiledScript cs = preLoad(script);
                        // 如果编译失败，从缓存中移除
                        if (cs == null) {
                            SCRIPT_CACHE.invalidate(key);
                            log.warn("脚本编译失败，已从缓存中移除,ID:{}", key);
                        }
                        return cs;
                    });
                } else {
                    log.warn("脚本执行参数错误:脚本为空");
                    return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本为空");
                }
            }
            SandboxThread sandboxThread = new SandboxThread(getEngine(), compiledScript, script, bindings.getBindings(),
                maxCPUTime, maxMemory);
            executorService.execute(sandboxThread);
            log.debug("开始监控脚本执行,ID:{}", id);
            sandboxThread.monitoring();
            if (sandboxThread.isCpuTimeExceeded()) {
                log.warn("脚本执行超时,ID:{},超时时间:{}ms", id, maxCPUTime);
                return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本执行超时（" + maxCPUTime + "ms）");
            } else if (sandboxThread.isMemoryExceeded()) {
                log.warn("脚本内存溢出,ID:{},限制内存:{}KB", id, maxMemory / 1024);
                return new ScriptRunnerResult().setSuccess(false)
                    .setErrorInfo("脚本内存溢出（" + maxMemory / 1024 + "KB）");
            }
            if (Objects.nonNull(sandboxThread.getException())) {
                log.error("脚本执行异常,ID:{},异常信息:{}", id, sandboxThread.getException().getMessage(),
                    sandboxThread.getException());
                return new ScriptRunnerResult().setSuccess(false)
                    .setErrorInfo(sandboxThread.getException().getMessage());
            }
            log.debug("脚本执行成功,ID:{}", id);
            return new ScriptRunnerResult(sandboxThread.getResult()).setConsoleInfo(
                new String(bindings.getOutputStream().toByteArray()));
        } catch (Exception e) {
            log.error("脚本执行失败,ID:{},脚本内容:{}", id, script, e);
            return new ScriptRunnerResult().setSuccess(false).setErrorInfo(e.getMessage());
        } finally {
            returnBindings(bindings);
        }
    }

    @Override
    public ScriptRunnerResult evalNoCache(String script, Object[] objects) {
        return evalMain(Md5Util.md5(script), script, false, objects);
    }

    @Override
    public Type getKey() {
        return Type.Nashorn;
    }

    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public String getCacheStats() {
        if (SCRIPT_CACHE != null) {
            return SCRIPT_CACHE.stats().toString();
        }
        return "缓存未初始化";
    }

    /**
     * 获取当前池大小
     * @return 当前Bindings池大小
     */
    public int getCurrentPoolSize() {
        return bindingsPool.size();
    }

    @Slf4j
    public static class RestrictedClassLoader extends ClassLoader {

        @Getter
        private Set<String> WHITE_LIST;
        @Setter
        private Set<String> BLACK_LIST;

        public RestrictedClassLoader(Set<String> whiteList, Set<String> blackList) {
            this.WHITE_LIST = whiteList;
            this.BLACK_LIST = blackList;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // 添加安全检查，防止加载危险类
            if (name != null && isClassInList(BLACK_LIST, name)) {
                log.error("尝试加载危险类:{}", name);
                throw new ClassNotFoundException("不允许加载危险类: " + name);
            }

            // 检查是否在白名单中（支持正则表达式匹配）
            if (!isClassInList(WHITE_LIST, name)) {
                log.warn("尝试加载未授权的类:{}", name);
                // 在生产环境中建议启用此行以增强安全性
                throw new ClassNotFoundException("不允许加载的类: " + name);
            }
            Class<?> clazz = super.loadClass(name, resolve);
            log.debug("成功加载类:{}", name);
            return clazz;
        }

        /**
         * 检查类是否在白名单中（支持正则表达式匹配）
         * @param className 类名
         * @return 是否在白名单中
         */
        private boolean isClassInList(Set<String> set, String className) {
            if (set.contains(className)) {
                return true;
            }
            // 支持正则表达式匹配，例如 java.lang.* 匹配 java.lang 包及其子包
            for (String pattern : set) {
                if (pattern.endsWith(".*")) {
                    // 处理包匹配，例如 java.lang.* 应该匹配 java.lang.String 等
                    String packagePrefix = pattern.substring(0, pattern.length() - 1); // 移除 *
                    if (className.startsWith(packagePrefix)) {
                        return true;
                    }
                } else if (pattern.contains("*")) {
                    // 处理更复杂的通配符模式
                    String regex = pattern.replace(".", "\\.").replace("*", ".*");
                    if (className.matches(regex)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
