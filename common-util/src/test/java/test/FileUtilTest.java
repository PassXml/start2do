package test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.jupiter.api.Test;
import org.start2do.util.FileUtil;

class FileUtilTest {

    @Test
    void test2() {
        FileUtil.writeString("S:\\123\\123", "1\n", StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        FileUtil.writeString("S:\\123\\123", "2\n", StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        FileUtil.writeString("S:\\123\\123", "3\n", StandardOpenOption.CREATE,StandardOpenOption.APPEND);
    }

    @Test
    void test1() throws IOException {
        Files.walkFileTree(
            Paths.get("D:\\project\\zx\\start2do3\\common-util\\src\\main\\java\\org\\start2do")
            , new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes)
                    throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
                    throws IOException {
                    System.out.println(path.getParent());
                    System.out.println(path.getFileName());
                    System.out.println("==================");
                    System.out.println(FileUtil.getDirname(path.getParent(), 0));
                    System.out.println(FileUtil.getDirname(path.getParent(), 1));
                    System.out.println(FileUtil.getDirname(path.getParent(), 2));
                    System.out.println(FileUtil.getDirname(path.getParent(), 3));
                    System.out.println("==================");
                    System.out.println(FileUtil.getFileName(path.getFileName().toString()));
                    System.out.println(FileUtil.getSuffix(path.getFileName().toString()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
    }
}
