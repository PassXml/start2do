package org.start2do.script.util;


import lombok.experimental.UtilityClass;
import org.start2do.script.IScriptRunner;
import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;

@UtilityClass
public class ScriptRunner {

    private static IScriptRunner INSTANCE;

    protected static void setINSTANCE(IScriptRunner INSTANCE) {
        ScriptRunner.INSTANCE = INSTANCE;
    }

    public static ScriptRunnerResult eval(ScriptRunnerInput input) {
        if (INSTANCE == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return eval(input.getScript(), input.getParams());
    }

    public static ScriptRunnerResult eval(String scprit, Object... objects) {
        if (INSTANCE == null) {
            throw new RuntimeException("没有配置脚本执行器");
        }
        return INSTANCE.eval(scprit, objects);
    }


}
