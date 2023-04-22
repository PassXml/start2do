package org.start2do;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.DatabaseConfigProvider;
import org.start2do.entity.HttpLog;
import org.start2do.entity.HttpLogId;

public class ClickHouseServerConfigProvider implements DatabaseConfigProvider {

    @Override
    public void apply(DatabaseConfig config) {
        if (EbeanClickHouseAutoConfig.ClickHouseEbeanDatabase.equals(config.getName())) {
            config.setDefaultServer(false);
            config.addClass(HttpLogId.class);
            config.addClass(HttpLog.class);
        }
    }
}
