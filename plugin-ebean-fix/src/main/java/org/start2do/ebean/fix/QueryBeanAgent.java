package org.start2do.ebean.fix;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * QueryBean Java Agent
 * 用于在运行时动态转换 QueryBean 字段访问为方法调用
 *
 * 使用方法:
 * java -javaagent:plugin-ebean-fix.jar YourMainClass
 */
public class QueryBeanAgent {

    /**
     * Agent 入口方法（JVM 启动时调用）
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("QueryBean Agent started - transforming QueryBean field access to method calls");
        inst.addTransformer(new QueryBeanClassFileTransformer());
    }

    /**
     * Agent 入口方法（动态附加时调用）
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("QueryBean Agent attached - transforming QueryBean field access to method calls");
        inst.addTransformer(new QueryBeanClassFileTransformer(), true);
    }

    /**
     * 类文件转换器
     */
    static class QueryBeanClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader,
                               String className,
                               Class<?> classBeingRedefined,
                               ProtectionDomain protectionDomain,
                               byte[] classfileBuffer) throws IllegalClassFormatException {

            // 跳过 JDK 核心类和 ASM 相关类
            if (className == null ||
                className.startsWith("java/") ||
                className.startsWith("javax/") ||
                className.startsWith("sun/") ||
                className.startsWith("org/objectweb/asm/")) {
                return null;
            }

            try {
                // 应用转换
                byte[] transformed = QueryBeanTransformer.transformClass(classfileBuffer);

                // 如果转换后的字节码与原始字节码不同，说明进行了转换
                if (transformed != null && !bytesEqual(transformed, classfileBuffer)) {
                    System.out.println("  [QueryBean Agent] Transformed: " + className.replace('/', '.'));
                    return transformed;
                }
            } catch (Exception e) {
                System.err.println("  [QueryBean Agent] Failed to transform: " + className);
                e.printStackTrace();
            }

            // 返回 null 表示不修改类
            return null;
        }

        /**
         * 比较两个字节数组是否相等
         */
        private boolean bytesEqual(byte[] a, byte[] b) {
            if (a.length != b.length) {
                return false;
            }
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
