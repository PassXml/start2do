package org.start2do.script;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;

public interface IScriptRunner<T> {

    Pattern IMPORT_PATTERN = Pattern.compile(
        "import\\s*(?:\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)|\\{([^}]*)\\}\\s*from\\s*['\"]([^'\"]+)['\"])\\s*;?",
        Pattern.MULTILINE);
    ThreadLocal<Set<String>> IMPORT_RESOLVING = ThreadLocal.withInitial(HashSet::new);


    ScriptRunnerResult eval(ScriptRunnerInput input);

    ScriptRunnerResult eval(String script, Object... params);

    ScriptRunnerResult evalById(String id, Object... params);

    /**
     * 预热脚本
     *
     * @return
     */
    T preLoad(String script);

    T preLoad(String id, String script);

    void clearAllCache();

    boolean hasCacheByScript(String script);

    boolean hasCacheById(String id);

    void removeById(String id);

    void removeByScript(String script);

    ScriptRunnerResult evalNoCache(String scirpt, Object[] objects);


    Type getKey();

    default void addWhiteList(String... classNames) {
    }

    default void addWhiteList(Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            return;
        }
        String[] classNames = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            classNames[i] = classes[i] == null ? null : classes[i].getName();
        }
        addWhiteList(classNames);
    }

    default void addFunction(Class<?>... functionClasses) {
    }

    default Map<String, Object> objectsToMap(Object... params) {
        Map<String, Object> map = new HashMap<>();
        if (params.length % 2 != 0) {
            throw new RuntimeException("参数个数必须为偶数");
        }
        for (int i = 0; i < params.length; i += 2) {
            Object param = params[i];

            map.put(String.valueOf(param), params[i + 1]);
        }
        return map;
    }

    /**
     * 解析脚本中的 import 语句，将其替换为对应函数库的源码。
     *
     * @param script         脚本源码
     * @param libraryRegistry 函数库注册表
     * @return 解析后的脚本源码
     */
    static String resolveImports(String script, Map<String, String> libraryRegistry) {
        if (script == null || script.isEmpty()) {
            return script;
        }

        Set<String> currentResolving = IMPORT_RESOLVING.get();
        Matcher matcher = IMPORT_PATTERN.matcher(script);
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String libName = matcher.group(1);
            if (libName == null) {
                libName = matcher.group(3);
            }

            if (currentResolving.contains(libName)) {
                throw new IllegalArgumentException(
                    "Circular import detected: " + libName + " is already being resolved");
            }

            String libSource = libraryRegistry.get(libName);
            if (libSource == null) {
                throw new IllegalArgumentException("Library not found: " + libName);
            }

            currentResolving.add(libName);
            try {
                String resolvedLibSource = resolveImports(libSource, libraryRegistry);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedLibSource));
            } finally {
                currentResolving.remove(libName);
            }
        }

        if (!found) {
            return script;
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 注册一个函数库，供脚本通过 import('name') 导入调用。
     *
     * @param name   库名称
     * @param script 库脚本源码
     */
    default void registerLibrary(String name, String script) {
    }

    /**
     * 批量注册函数库。
     */
    default void registerLibraries(Map<String, String> libraries) {
    }

    /**
     * 移除指定名称的函数库。
     */
    default void removeLibrary(String name) {
    }

    /**
     * 获取所有已注册的库名称。
     */
    default Set<String> getLibraryNames() {
        return Collections.emptySet();
    }
}
