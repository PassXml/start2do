package org.start2do.ebean;


import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.start2do.util.StringUtils;

@UtilityClass
public class CreateSql {

    @SneakyThrows
    public void run(String path, String name, String version, Platform platform) {
        if (StringUtils.isEmpty(path)) {
            String root = Paths.get("").toFile().getAbsolutePath();
            Path path1 = Paths.get(root + "/sql");
            path1.toFile().mkdirs();
            path = path1.toString();
        }
        DbMigration dbMigration = DbMigration.create();
        dbMigration.setVersion(version);
        dbMigration.setName(name);
        dbMigration.setPathToResources(path);
        dbMigration.setPlatform(platform);
        dbMigration.generateMigration();
    }
}
