package org.start2do;

import io.ebean.DatabaseBuilder;
import io.ebean.DatabaseBuilder.Settings;
import io.ebean.config.DatabaseConfigProvider;
import org.start2do.entity.HttpLog;
import org.start2do.entity.HttpLogId;

public class ClickHouseServerConfigProvider implements DatabaseConfigProvider {


    @Override
    public void apply(DatabaseBuilder builder) {
        Settings config = builder.settings();
        if (EbeanClickHouseAutoConfig.ClickHouseEbeanDatabase.equals(config.getName())) {
            config.setDefaultServer(false);
            config.addClass(HttpLogId.class);
            config.addClass(HttpLog.class);
        }
    }
}
