package org.start2do.ebean.fix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Gradle 插件任务处理器
 * 用于在 Gradle 构建过程中应用 QueryBean 转换
 */
public class QueryBeanGradlePlugin {

    /**
     * 转换编译后的类文件
     *
     * @param buildOutputDir 构建输出目录，通常是 build/classes/java/main
     */
    public static void transformBuildOutput(File buildOutputDir) {
        if (!buildOutputDir.exists() || !buildOutputDir.isDirectory()) {
            System.out.println("Build output directory does not exist: " + buildOutputDir);
            return;
        }

        try {
            System.out.println("Starting QueryBean field-to-method transformation...");
            System.out.println("Scanning directory: " + buildOutputDir.getAbsolutePath());

            transformDirectory(buildOutputDir.toPath());
        } catch (IOException e) {
            System.err.println("QueryBean transformation failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to transform QueryBean classes", e);
        }
    }

    /**
     * 递归转换目录中的所有 class 文件
     *
     * @param dirPath 目录路径
     * @return 转换的文件数量
     * @throws IOException IO 异常
     */
    private static int transformDirectory(Path dirPath) throws IOException {
        int[] totalCount = {0};
        int[] modifiedCount = {0};

        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".class"))
                 .forEach(path -> {
                     try {
                         File classFile = path.toFile();
                         long sizeBefore = classFile.length();

                         QueryBeanTransformer.transformClassFile(classFile);

                         long sizeAfter = classFile.length();
                         totalCount[0]++;

                         // 只输出被修改的文件
                         if (sizeBefore != sizeAfter) {
                             modifiedCount[0]++;
                             System.out.println("  ✓ " + path.getFileName() +
                                 " (before: " + sizeBefore + " bytes, after: " + sizeAfter + " bytes)");
                         }
                     } catch (IOException e) {
                         System.err.println("  ✗ Failed to transform: " + path);
                         e.printStackTrace();
                     }
                 });
        }

        // 输出统计信息
        if (modifiedCount[0] > 0) {
            System.out.println("QueryBean transformation completed. " +
                "Scanned " + totalCount[0] + " files, modified " + modifiedCount[0] + " files.");
        } else {
            System.out.println("QueryBean transformation completed. " +
                "Scanned " + totalCount[0] + " files, no QueryBean field access found.");
        }

        return totalCount[0];
    }
}
