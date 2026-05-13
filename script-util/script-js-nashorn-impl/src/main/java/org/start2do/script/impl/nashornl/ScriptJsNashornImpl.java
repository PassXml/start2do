package org.start2do.script.impl.nashornl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
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
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.BindingDto;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.util.ScriptWhiteList;
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
    private RestrictedClassLoader restrictedClassLoader;

    private final Map<String, String> libraryRegistry = new ConcurrentHashMap<>();

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

    public ScriptJsNashornImpl(Set<String> clazzList, Cache<String, CompiledScript> caffeine, String globalScript,
        long maxCPUTime, long maxMemory, int maxPoolSize) {
        this.MAX_POOL_SIZE = maxPoolSize;
        ScriptJsNashornImpl.INSTANCE = this;
        if (clazzList == null) {
            clazzList = new HashSet<>();
        }
        this.maxCPUTime = maxCPUTime;
        this.maxMemory = maxMemory;
        init(clazzList, caffeine, globalScript);

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

    /**
     * 将传入脚本包裹为 IIFE（立即执行函数）。
     * 作用：为每次执行提供独立的词法作用域，避免 ES6 的 const/let 在同一 Global 环境下二次执行时报错。
     */
    private String wrapInIIFE(String code) {
        if (code == null) {
            return null;
        }
        return "(function(){\n" + code + "\n})();";
    }

    private void init(Set<String> set, Cache<String, CompiledScript> caffeine, String globalScript) {
        ScriptJsNashornImpl.INSTANCE = this;
        SCRIPT_CACHE = caffeine;
        executorService = new ThreadPoolExecutor(2, MAX_POOL_SIZE, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());
        Set<String> mergedWhiteList = new LinkedHashSet<>(ScriptWhiteList.defaultWhiteList());
        if (set != null) {
            mergedWhiteList.addAll(set);
        }
        mergedWhiteList.add("okhttp3.OkHttpClient.Builder");
        mergedWhiteList.add("org.openjdk.nashorn.api.linker.NashornLinkerExporter");
        mergedWhiteList.add("org.start2do.script.util.impl.functions.CustomConsole");
        restrictedClassLoader = new RestrictedClassLoader(new CopyOnWriteArraySet<>(mergedWhiteList));
        this.engine = new NashornScriptEngineFactory().getScriptEngine(new String[]{"--language=es6"},
            restrictedClassLoader);
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
        maxMemory = 100 * 1024 * 1024;
        maxCPUTime = 60 * 1000 * 3;
        this.MAX_POOL_SIZE = 20;
        init(new HashSet<>(ScriptWhiteList.defaultWhiteList()),
            Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build(), null);
    }

    @Override
    public ScriptRunnerResult eval(ScriptRunnerInput input) {
        String script = resolveImports(input.getScript());
        return evalMain(input.getId(), script, true, input.getParams());
    }

    @Override
    public ScriptRunnerResult eval(String script, Object... params) {
        script = resolveImports(script);
        return evalMain(null, script, true, params);
    }

    @Override
    public ScriptRunnerResult evalById(String id, Object... params) {
        return evalMain(id, null, true, params);
    }

    @Override
    public CompiledScript preLoad(String script) {
        script = resolveImports(script);
        return preLoad(Md5Util.md5(script), script);
    }

    @Override
    public CompiledScript preLoad(String id, String script) {
        script = resolveImports(script);
        try {
            // 为避免 ES6 的 const/let 在同一引擎全局环境下二次执行报"已声明"错误，
            // 将脚本包装到 IIFE（立即执行函数）中，保证每次执行拥有独立词法作用域。
            String toCompile = StringUtils.isNotEmpty(GLOBAL_SCRIPT) ? GLOBAL_SCRIPT + script : script;
            CompiledScript compile = ((Compilable) engine).compile(wrapInIIFE(toCompile));
            SCRIPT_CACHE.put(id, compile);
            return compile;
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
            CompiledScript compiledScript = null;
            if (isCache) {
                if (StringUtils.isEmpty(id) && StringUtils.isEmpty(script)) {
                    return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本为空或id为空");
                }
                if (StringUtils.isNotEmpty(id)) {
                    compiledScript = SCRIPT_CACHE.getIfPresent(id);
                } else if (StringUtils.isNotEmpty(script)) {
                    String key = Md5Util.md5(script);
                    compiledScript = SCRIPT_CACHE.getIfPresent(key);
                    if (compiledScript == null) {
                        compiledScript = preLoad(script);
                    }
                } else {
                    return new ScriptRunnerResult().setSuccess(false).setErrorInfo("脚本为空");
                }
            }
            // 非缓存或未命中缓存时，直接执行脚本也需要包裹 IIFE，另外保持与缓存模式一致的 GLOBAL_SCRIPT 注入
            if (compiledScript == null && StringUtils.isNotEmpty(script)) {
                String exec = StringUtils.isNotEmpty(GLOBAL_SCRIPT) ? GLOBAL_SCRIPT + script : script;
                script = wrapInIIFE(exec);
            }
            SandboxThread sandboxThread = new SandboxThread(getEngine(), compiledScript, script, bindings.getBindings(),
                maxCPUTime, maxMemory);
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
                new String(bindings.getOutputStream().toByteArray()));
        } catch (Exception e) {
            log.error("脚本执行失败,{},{}", id, script, e);
            return new ScriptRunnerResult().setSuccess(false).setErrorInfo(e.getMessage());
        } finally {
            returnBindings(bindings);
        }
    }

    @Override
    public ScriptRunnerResult evalNoCache(String script, Object[] objects) {
        script = resolveImports(script);
        return evalMain(Md5Util.md5(script), script, false, objects);
    }

    @Override
    public Type getKey() {
        return Type.Nashorn;
    }

    @Override
    public void addWhiteList(String... classNames) {
        if (restrictedClassLoader == null || classNames == null || classNames.length == 0) {
            return;
        }
        for (String className : classNames) {
            if (StringUtils.isNotEmpty(className)) {
                restrictedClassLoader.getWHITE_LIST().add(className);
            }
        }
    }

    @Override
    public void registerLibrary(String name, String script) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Library name must not be empty");
        }
        libraryRegistry.put(name, script);
        log.info("Registered library: {}", name);
    }

    @Override
    public void registerLibraries(Map<String, String> libraries) {
        if (libraries != null) {
            libraryRegistry.putAll(libraries);
            log.info("Registered {} libraries", libraries.size());
        }
    }

    @Override
    public void removeLibrary(String name) {
        libraryRegistry.remove(name);
    }

    @Override
    public Set<String> getLibraryNames() {
        return new CopyOnWriteArraySet<>(libraryRegistry.keySet());
    }

    /**
     * 解析脚本中的 import('libName') 语句，将其替换为对应函数库的源码。
     */
    String resolveImports(String script) {
        return IScriptRunner.resolveImports(script, libraryRegistry);
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
            // 支持白名单通配符与内部类判断
            // 1. 通配符说明：
            //    - "*"   匹配单段（不跨越 '.' 与 '$'）
            //    - "**"  跨段通配（可跨越多个 '.' 或 '$'）
            // 2. 内部类支持：未直接匹配到时，自动以外部类名（去除'$'之后部分）再匹配一次
            // 3. Spring Boot 兼容：白名单通过后，按 TCCL → 组件CL → 当前CL → 系统CL → super.loadClass 顺序尝试
            synchronized (getClassLoadingLock(name)) {
                // 避免重复加载
                Class<?> loaded = findLoadedClass(name);
                if (loaded != null) {
                    if (resolve) {
                        resolveClass(loaded);
                    }
                    return loaded;
                }

                if (!isAllowedByWhitelist(name)) {
                    log.warn("不允许加载的类:{}", name);
                    throw new ClassNotFoundException("不允许加载的类" + name);
                }

                Class<?> clazz = null;
                // 依次尝试多种类加载器，提升 Spring Boot 环境兼容性
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                ClassLoader componentCl = ScriptJsNashornImpl.class.getClassLoader();
                ClassLoader currentCl = this.getClass().getClassLoader();
                ClassLoader sysCl = getSystemClassLoader();

                clazz = tryLoadBy("TCCL", name, tccl);
                if (clazz == null) clazz = tryLoadBy("ComponentCL", name, componentCl);
                if (clazz == null) clazz = tryLoadBy("CurrentCL", name, currentCl);
                if (clazz == null) clazz = tryLoadBy("SystemCL", name, sysCl);
                if (clazz == null) {
                    try {
                        clazz = super.loadClass(name, false);
                    } catch (ClassNotFoundException ignore) {
                    }
                }

                if (clazz == null) {
                    throw new ClassNotFoundException("未找到类（已通过白名单）:" + name);
                }
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        }

        private Class<?> tryLoadBy(String tag, String name, ClassLoader cl) {
            if (cl == null) {
                return null;
            }
            try {
                return Class.forName(name, false, cl);
            } catch (ClassNotFoundException e) {
                return null;
            } catch (LinkageError e) {
                // 若发生链接错误，交由上层继续其它加载器尝试，避免中断
                return null;
            }
        }

        /**
         * 白名单匹配：支持精确、通配（* / **）与内部类（$）
         */
        private boolean isAllowedByWhitelist(String className) {
            if (WHITE_LIST == null || WHITE_LIST.isEmpty()) {
                return false;
            }
            // 先尝试精确匹配
            if (WHITE_LIST.contains(className)) {
                return true;
            }
            // 内部类：尝试以外部类名匹配（去掉'$'及其后缀）
            String outerClassName = null;
            int dollarIdx = className.indexOf('$');
            if (dollarIdx > 0) {
                outerClassName = className.substring(0, dollarIdx);
                if (WHITE_LIST.contains(outerClassName)) {
                    return true;
                }
            }

            // 通配符匹配
            for (String pattern : WHITE_LIST) {
                if (pattern == null) {
                    continue;
                }
                if (pattern.indexOf('*') < 0) {
                    // 非通配：已在 contains 检查过，跳过
                    continue;
                }
                if (wildcardMatch(pattern, className)) {
                    return true;
                }
                if (outerClassName != null && wildcardMatch(pattern, outerClassName)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 将白名单通配符转换为正则并匹配。
         * 规则：
         *  - '.'  -> 作为分隔符，匹配实际类名中的 '.' 或 '$'（兼容内部类二进制名）
         *  - '*'  -> 单段匹配：不跨越分隔符（'.' 或 '$'）的任意字符
         *  - '**' -> 跨段匹配：任意字符（可跨越 '.' 与 '$'）
         *
         * 说明：Nashorn 在 Class 解析时会将右侧分段逐步由 '.' 改为 '$' 以尝试内部类加载，
         *      因此这里将模式中的 '.' 视为分隔符，允许匹配两种实际形式，
         *      使诸如 "org.start2do.util.*" 能匹配到 "org.start2do.util$DateUtil"。
         */
        private boolean wildcardMatch(String pattern, String className) {
            String regex = toRegexFromPattern(pattern);
            return className.matches(regex);
        }

        /**
         * 将类似 org.start2do.**.* 的模式转换为正则：
         *  - 正则敏感字符转义（保留 '*' 处理）
         *  - '.'  视为分隔符，转换为 (?:\\.|\\$)
         *  - '**' 转为跨段匹配 [\\s\\S]*（包含 '.' 与 '$'）
         *  - '*'  转为单段匹配 [^\\.\\$]*（不跨越分隔符）
         */
        private String toRegexFromPattern(String pattern) {
            StringBuilder sb = new StringBuilder();
            char[] arr = pattern.toCharArray();
            for (int i = 0; i < arr.length; i++) {
                char c = arr[i];
                switch (c) {
                    case '.':
                        // 将模式中的分隔符 '.' 同时匹配实际类名中的 '.' 或 '$'
                        sb.append("(?:\\.|\\$)");
                        break;
                    case '$':
                        sb.append("\\$");
                        break;
                    case '*':
                        if (i + 1 < arr.length && arr[i + 1] == '*') {
                            // '**' -> 跨段（包含 '.' 与 '$'）的任意字符
                            sb.append("[\\s\\S]*");
                            i++; // 跳过第二个 '*'
                        } else {
                            // '*' -> 单段（不包含 '.' 与 '$'）
                            sb.append("[^\\.\\$]*");
                        }
                        break;
                    case '+': case '?': case '^': case '{': case '}':
                    case '(': case ')': case '[': case ']': case '|': case '\\':
                        sb.append('\\').append(c);
                        break;
                    default:
                        sb.append(c);
                }
            }
            return "^" + sb + "$";
        }
    }
}
