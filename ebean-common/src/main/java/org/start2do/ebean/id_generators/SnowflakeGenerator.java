package org.start2do.ebean.id_generators;

import io.ebean.config.IdGenerator;
import org.start2do.ebean.util.Snowflake;

public class SnowflakeGenerator implements IdGenerator {

    private final Snowflake snowflake;
    public static final String KEY = "snowflake";

    @Override
    public Object nextValue() {
        return this.snowflake.nextId();
    }

    @Override
    public String getName() {
        return "snowflake";
    }

    public SnowflakeGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }
}
