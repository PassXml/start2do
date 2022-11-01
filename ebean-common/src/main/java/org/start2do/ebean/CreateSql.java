package org.start2do.ebean;


import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.SneakyThrows;
import org.start2do.util.StringUtils;

public abstract class CreateSql {

    @SneakyThrows
    public static void run(String path, String name, String version, Platform platform) {
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

    @SneakyThrows
    public static void init(String pathStr, Platform platform) {
        Path path = Paths.get(pathStr + "dbmigration");
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return super.visitFile(file, attrs);
                }
            });
        }
        run(pathStr, "init", "1.0", platform);
    }
}
