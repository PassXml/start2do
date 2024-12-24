package org.start2do.script;


import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;

public interface IScriptRunner {

    ScriptRunnerResult eval(ScriptRunnerInput input);

    ScriptRunnerResult eval(String script, Object... params);


}
