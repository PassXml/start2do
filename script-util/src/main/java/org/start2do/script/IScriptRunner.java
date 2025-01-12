package org.start2do.script;


import org.start2do.script.dto.ScriptRunnerInput;
import org.start2do.script.dto.ScriptRunnerResult;

public interface IScriptRunner<T> {


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

    ScriptRunnerResult evalNoCache(String scprit, Object[] objects);
}
