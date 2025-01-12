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

    /**
     * 没有的话,默认ID
     */
    private String id;
    private Value script;
    private ByteArrayOutputStream consoleInfo = new ByteArrayOutputStream();
    private ByteArrayOutputStream errorInfo = new ByteArrayOutputStream();

    public ScriptJsCache(String scriptMd5) {
        this.id = scriptMd5;
    }

    public ScriptJsCache(String id, Value script) {
        this.id = id;
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
