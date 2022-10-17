package org.start2do;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateDDLSQL {

    public static void main(String[] args) throws IOException {
        String root = Paths.get("").toFile().getAbsolutePath();
        Path path = Paths.get(root + "/spring-security-api/src/main/resources");
        DbMigration dbMigration = DbMigration.create();
        dbMigration.setVersion("1.0");
        dbMigration.setName("initModel");
        dbMigration.setPathToResources(path.toString());
        dbMigration.setPlatform(Platform.ORACLE);
//        dbMigration.setPlatform(Platform.MYSQL);
        dbMigration.generateMigration();
    }
}
