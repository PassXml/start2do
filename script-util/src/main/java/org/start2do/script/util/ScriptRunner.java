package org.start2do.script.util;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import org.start2do.script.IScriptRunner;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.util.Md5Util;

@UtilityClass
public class ScriptRunner {

    private static Type defaultImpl;
    private static Map<Type, IScriptRunner> map = new ConcurrentHashMap<>();
//    private static IScriptRunner INSTANCE;

    public <T extends IScriptRunner> T getInstance() {
        return (T) map.get(defaultImpl);
    }

    public <T extends IScriptRunner> T getInstance(String impl) {
        return (T) map.get(impl);
    }

    public Type getDefaultImpl() {
        return defaultImpl;
    }

    public Set<Type> getAllImplKey() {
        return map.keySet();
    }

    public void addImpl(IScriptRunner runner) {
        if (runner != null) {
            map.put(runner.getKey(), runner);
        }
    }

    public static void setDefaultInstance(IScriptRunner INSTANCE) {
        defaultImpl = INSTANCE.getKey();
        map.put(defaultImpl, INSTANCE);
    }

    public static ScriptRunnerResult eval(ScriptRunnerInput input) {
        return evalByImpl(defaultImpl, input);
    }

    public static ScriptRunnerResult evalByImpl(Type implKey, ScriptRunnerInput input) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return runner.eval(input.getScript(), input.getParams());
    }

    public static boolean checkScriptByScript(String script) {
        return checkScriptByScriptByImpl(defaultImpl, script);
    }

    public static boolean checkScriptByScriptByImpl(Type implKey, String script) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return runner.hasCacheByScript(script);
    }

    public static boolean checkScriptById(String id) {
        return checkScriptById(defaultImpl, id);
    }

    public static boolean checkScriptById(Type implKey, String id) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return runner.hasCacheById(id);
    }

    public static ScriptRunnerResult evalById(String id, Object... objects) {
        return evalById(defaultImpl, id, objects);
    }

    public static ScriptRunnerResult evalById(Type implKey, String id, Object... objects) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return runner.evalById(id, objects);
    }

    public static ScriptRunnerResult evalNoCache(String script, Object... objects) {
        return evalNoCache(defaultImpl, script, objects);
    }

    public static ScriptRunnerResult evalNoCache(Type implKey, String scprit, Object... objects) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return runner.evalNoCache(scprit, objects);
    }

    public static ScriptRunnerResult eval(Type script, Object... objects) {
        return eval(defaultImpl, script, objects);
    }

    public static ScriptRunnerResult eval(String implKey, String scprit, Object... objects) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return runner.eval(scprit, objects);
    }

    public void preLoad(String script) {
        preLoad(defaultImpl, script);
    }


    public void preLoad(Type implKey, String script) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        runner.preLoad(script);
    }

    public void preLoadByDefaultImpl(String id, String script) {
        preLoad(defaultImpl, id, script);
    }

    public void preLoad(Type implKey, String id, String script) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        runner.preLoad(id, script);
    }

    public void clearAllCache() {
        clearAllCache(defaultImpl);
    }

    public void clearAllCache(Type implKey) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        runner.clearAllCache();
    }

    public void removeById(String id) {
        removeById(defaultImpl, id);
    }

    public void removeByScript(String script) {
        removeById(defaultImpl, Md5Util.md5(script));
    }

    public void removeById(Type implKey, String id) {
        IScriptRunner runner = map.get(implKey);
        if (runner == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        runner.removeById(id);
    }

}
