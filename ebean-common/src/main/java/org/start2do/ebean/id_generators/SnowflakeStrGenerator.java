package org.start2do.ebean.id_generators;


import io.ebean.config.IdGenerator;
import org.start2do.ebean.util.Snowflake;

public class SnowflakeStrGenerator implements IdGenerator {
    private final Snowflake snowflake;
    public static final String KEY = "snowflakeStr";

    @Override
    public Object nextValue() {
        return String.valueOf(this.snowflake.nextId());
    }

    @Override
    public String getName() {
        return "snowflakeStr";
    }

    public SnowflakeStrGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }
}
