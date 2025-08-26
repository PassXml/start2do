package org.start2do.script.util.impl.functions;

import lombok.experimental.UtilityClass;
import org.start2do.util.Snowflake;

@UtilityClass
public class SnowflakeStrGenerator {

    private Snowflake snowflake = new Snowflake(1);

    public void resetNodeId(Integer id) {
        snowflake = new Snowflake(id);
    }

    public String nextId() {
        return String.valueOf(snowflake.nextId());
    }

}
