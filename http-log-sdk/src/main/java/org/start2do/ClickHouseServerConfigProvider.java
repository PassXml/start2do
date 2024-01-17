package org.start2do;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfigProvider;
import org.start2do.entity.HttpLog;
import org.start2do.entity.HttpLogId;

public class ClickHouseServerConfigProvider implements DatabaseConfigProvider {


    @Override
    public void apply(DatabaseBuilder databaseBuilder) {
        databaseBuilder.name(EbeanClickHouseAutoConfig.ClickHouseEbeanDatabase);
        databaseBuilder.addClass(HttpLogId.class);
        databaseBuilder.addClass(HttpLog.class);
        databaseBuilder.defaultDatabase(false);

    }
}
