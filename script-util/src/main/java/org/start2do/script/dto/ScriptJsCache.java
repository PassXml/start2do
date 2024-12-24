package org.start2do.script.dto;

import java.io.ByteArrayOutputStream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.graalvm.polyglot.Value;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ScriptJsCache {

    private String scriptMd5;
    private Value script;
    private ByteArrayOutputStream consoleInfo = new ByteArrayOutputStream();
    private ByteArrayOutputStream errorInfo = new ByteArrayOutputStream();

    public ScriptJsCache(String scriptMd5) {
        this.scriptMd5 = scriptMd5;
    }

    public ScriptJsCache(String scriptMd5, Value script) {
        this.scriptMd5 = scriptMd5;
        this.script = script;
    }

    public String getConsoleOutInfo() {
        String string = consoleInfo.toString();
        consoleInfo.reset();
        return string;
    }

    public String getErrorOutInfo() {
        String errorInfoString = errorInfo.toString();
        errorInfo.reset();
        return errorInfoString;
    }
}
