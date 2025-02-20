package org.start2do.script.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class ScriptRunnerResult {

    private boolean success;
    private Object result;
    private String consoleInfo;
    private String errorInfo;

    public ScriptRunnerResult(Object result) {
        this.success = true;
        this.result = result;
    }

    public static ScriptRunnerResult fail(String errorInfo) {
        ScriptRunnerResult result = new ScriptRunnerResult();
        result.setSuccess(false);
        result.setErrorInfo(errorInfo);
        return result;
    }
}
