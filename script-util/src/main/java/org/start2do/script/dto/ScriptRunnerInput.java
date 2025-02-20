package org.start2do.script.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ScriptRunnerInput {

    private String id;
    private String script;
    private Object[] params;

    public static ScriptRunnerInput buildById(String id, Object... params) {
        return new ScriptRunnerInput().setId(id).setParams(params);
    }

    public static ScriptRunnerInput buildByScript(String script, Object... params) {
        return new ScriptRunnerInput().setScript(script).setParams(params);
    }

}
