package org.start2do.script.util;


import lombok.experimental.UtilityClass;
import org.start2do.script.IScriptRunner;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;

@UtilityClass
public class ScriptRunner {

    private static IScriptRunner INSTANCE;

    public <T extends IScriptRunner> T getInstance() {
        return (T) INSTANCE;
    }

    protected static void setINSTANCE(IScriptRunner INSTANCE) {
        ScriptRunner.INSTANCE = INSTANCE;
    }

    public static ScriptRunnerResult eval(ScriptRunnerInput input) {
        if (INSTANCE == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return eval(input.getScript(), input.getParams());
    }

    public static boolean checkScriptByScript(String script) {
        return INSTANCE.hasCacheByScript(script);

    }

    public static boolean checkScriptById(String id) {
        return INSTANCE.hasCacheById(id);
    }

    public static ScriptRunnerResult evalById(String id, Object... objects) {
        return INSTANCE.evalById(id, objects);
    }

    public static ScriptRunnerResult evalNoCache(String scprit, Object... objects) {
        return INSTANCE.evalNoCache(scprit, objects);
    }

    public static ScriptRunnerResult eval(String scprit, Object... objects) {
        if (INSTANCE == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return INSTANCE.eval(scprit, objects);
    }

    public void preLoad(String script) {
        INSTANCE.preLoad(script);
    }

    public void preLoad(String id, String script) {
        INSTANCE.preLoad(id, script);
    }

    public void clearAllCache() {
        INSTANCE.clearAllCache();
    }

}
