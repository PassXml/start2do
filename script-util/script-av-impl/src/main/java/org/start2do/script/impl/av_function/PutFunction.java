package org.start2do.script.impl.av_function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import java.util.Map;

/**
 * @author lijie
 */
public class PutFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "put";
    }

    /**
     * <pre>
     * if column=='DEPTID' && data=='D13'{
     * put(result,column,'D013')
     * }
     *  </pre>
     */
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2,
        AviatorObject arg3) {
        Object value = arg1.getValue(env);
        if (value instanceof Map) {
            Map map = (Map) value;
            map.put(arg2.getValue(env), arg3.getValue(env));
            return AviatorBoolean.TRUE;
        }
        return super.call(env, arg1, arg2, arg3);
    }

}
