package org.start2do.ebean.fix;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * QueryBean 转换工具类
 * 提供字节码转换的核心功能
 */
public class QueryBeanTransformer {

    /**
     * 转换单个类的字节码
     *
     * @param classBytes 原始类字节码
     * @return 转换后的类字节码
     */
    public static byte[] transformClass(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new QueryBeanClassVisitor(Opcodes.ASM9, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    /**
     * 转换单个 class 文件
     *
     * @param classFile 类文件
     * @throws IOException IO 异常
     */
    public static void transformClassFile(File classFile) throws IOException {
        byte[] classBytes = Files.readAllBytes(classFile.toPath());
        byte[] transformedBytes = transformClass(classBytes);

        try (FileOutputStream fos = new FileOutputStream(classFile)) {
            fos.write(transformedBytes);
        }
    }

    /**
     * 递归转换目录下的所有 class 文件
     *
     * @param directory 目录路径
     * @throws IOException IO 异常
     */
    public static void transformDirectory(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        if (!Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".class"))
                 .forEach(path -> {
                     try {
                         transformClassFile(path.toFile());
                         System.out.println("Transformed: " + path);
                     } catch (IOException e) {
                         System.err.println("Failed to transform: " + path);
                         e.printStackTrace();
                     }
                 });
        }
    }

    /**
     * 从输入流读取类字节码并转换
     *
     * @param inputStream 输入流
     * @return 转换后的字节码
     * @throws IOException IO 异常
     */
    public static byte[] transformClass(InputStream inputStream) throws IOException {
        byte[] classBytes = inputStream.readAllBytes();
        return transformClass(classBytes);
    }

    /**
     * 命令行入口
     * 用法: java QueryBeanTransformer <directory>
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java QueryBeanTransformer <directory>");
            System.err.println("  <directory>: The directory containing .class files to transform");
            System.exit(1);
        }

        String directory = args[0];
        try {
            System.out.println("Transforming classes in directory: " + directory);
            transformDirectory(directory);
            System.out.println("Transformation completed successfully.");
        } catch (Exception e) {
            System.err.println("Transformation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
