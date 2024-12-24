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

    private String script;
    private Object[] params;

    public ScriptRunnerInput(String script, Object... params) {
        this.script = script;
        this.params = params;
    }
}
