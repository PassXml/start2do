package org.start2do;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.start2do.script.dto.ScriptRunnerResult;
import org.start2do.script.util.impl.ScriptRunnerAvImpl;
import org.start2do.script.util.impl.ScriptRunnerJsImpl;

public class TestMain {

    @Test
    public void jsTest() throws JsonProcessingException {
        String script = """
            function add(a, b) { return a + b; }
            function sub(a,b){return a-b;  }
            function main(args){
                var JavaPI = Java.type('java.lang.Math').PI;
            if (args['a']%2==0){console.log('你好');}
                return {'b':args['a'],'a':JavaPI,'c':args['a']%2,'d':run.getInfo(JString.valueOf(args['a']),2)};
            }
            """;
        ObjectMapper objectMapper = new ObjectMapper();
        ScriptRunnerJsImpl runnerJs = new ScriptRunnerJsImpl(Arrays.asList(StaticExecutor.class), null, null, """
            var run=Java.type('org.start2do.StaticExecutor');
            """);
        for (int i = 0; i < 1000; i++) {
            System.out.println(objectMapper.writeValueAsString(runnerJs.eval(script, Map.of("a", i, "b", 1))));
        }
    }

    @Test
    public void avTest() {
        ScriptRunnerAvImpl av = new ScriptRunnerAvImpl();
        ScriptRunnerResult result = av.eval("""
                 log("ABC\r\n");
                 return  a+c;        
            
            """, "a", "2", "c", "3");
        System.out.println(result);
    }
}
