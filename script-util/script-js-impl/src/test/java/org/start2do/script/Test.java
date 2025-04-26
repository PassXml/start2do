package org.start2do.script;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.start2do.script.ScriptRunnerConfiguration.Type;
import org.start2do.script.config.GraalJsConfig;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.util.ScriptRunner;

@Slf4j
public class Test {

    @org.junit.jupiter.api.Test
    public void JsTest() {
        ScriptRunnerJsAutoConfiguration configuration = new ScriptRunnerJsAutoConfiguration();
        IScriptRunner runner = configuration.js(
            new ScriptRunnerConfiguration().setEnable(true).setDefaultRunner(Type.GraalJS),
            new GraalJsConfig().setEnable(true).setExpireAfterAccess(
                Duration.ofMinutes(15))
        );
        ScriptRunnerResult result = ScriptRunner.eval("""   
             function add(a,b){
                  return a+b;
              }
            return  add(a,b);
            """, "a", 12, "b", "2");
        log.info("{}", result);
    }
}
