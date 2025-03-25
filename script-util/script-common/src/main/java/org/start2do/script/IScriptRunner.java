package org.start2do.script;


import java.util.HashMap;
import java.util.Map;
import org.start2do.script.ScriptRunnerConfiguration.Type;
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

    ScriptRunnerResult evalNoCache(String scirpt, Object[] objects);


    Type getKey();

    default Map<String, Object> objectsToMap(Object... params) {
        Map<String, Object> map = new HashMap<>();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                map.put(String.valueOf(i), params[i]);
            }
        }
        return map;
    }
}
