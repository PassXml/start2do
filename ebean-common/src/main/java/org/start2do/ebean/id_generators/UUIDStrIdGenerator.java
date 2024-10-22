package org.start2do.ebean.id_generators;

import io.ebean.config.IdGenerator;
import java.util.UUID;

public class UUIDStrIdGenerator implements IdGenerator {

    public static String KEY = "UUIDStr";

    @Override
    public Object nextValue() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public String getName() {
        return KEY;
    }
}
