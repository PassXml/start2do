package org.start2do.ebean.id_generators;

import io.ebean.config.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.start2do.constant.Constant;
import org.start2do.util.Snowflake;

@RequiredArgsConstructor
public class SnowflakeGenerator implements IdGenerator {

    private final Snowflake snowflake;

    public static final String KEY = Constant.ID_GENERATOR_SNOW_FLAKE;

    @Override
    public Object nextValue() {
        return snowflake.nextId();
    }

    @Override
    public String getName() {
        return KEY;
    }
}
