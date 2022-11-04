package org.start2do.ebean;


import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.CRC32;
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

    public static int calculate(String str) {
        final CRC32 crc32 = new CRC32();
        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
                crc32.update(lineBytes, 0, lineBytes.length);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
        return (int) crc32.getValue();
    }
}
