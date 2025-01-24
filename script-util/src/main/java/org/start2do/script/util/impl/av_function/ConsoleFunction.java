package org.start2do.script.util.impl.av_function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.start2do.script.util.impl.functions.JacksonOperateFunction;

@Slf4j
public class ConsoleFunction extends AbstractFunction {

    public static final String CONSOLEKEY = "_CONSOLE_INFO_";
    private static final byte[] bytes = "\r\n".getBytes(StandardCharsets.UTF_8);

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        if (env.containsKey(CONSOLEKEY)) {
            Object object = env.get(CONSOLEKEY);
            if (object instanceof OutputStream) {
                Object consoleInfo = arg1.getValue(env);
                try {
                    OutputStream outputStream = (OutputStream) object;
                    if (consoleInfo instanceof String) {
                        outputStream.write(((String) consoleInfo).getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } else if (consoleInfo != null) {
                        outputStream.write(JacksonOperateFunction.toBytes(consoleInfo));
                        outputStream.write(bytes);
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    log.error("写入控制台失败,{}", e.getMessage());
                }

            }
        }
        return AviatorNil.NIL;
    }

}
