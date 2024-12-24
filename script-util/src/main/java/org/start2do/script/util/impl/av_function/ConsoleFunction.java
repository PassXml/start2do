package org.start2do.script.util.impl.av_function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleFunction extends AbstractFunction {

    private String CONSOLEKEY = "_LOG";

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Object value = arg1.getValue(env);
        if (env.containsKey(CONSOLEKEY)) {
            Object consoleOut = env.get(CONSOLEKEY);
            consoleOut += String.valueOf(value);
            env.put(CONSOLEKEY, consoleOut);
            HashMap<String, Object> map = new HashMap<>();
            map.put("log", consoleOut);
            return AviatorRuntimeJavaType.valueOf(
                map
            );
        } else {
            env.put(CONSOLEKEY, String.valueOf(value));
        }
        return AviatorRuntimeJavaType.valueOf(new HashMap<>());
    }

}
