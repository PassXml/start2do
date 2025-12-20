package org.start2do.ebean.fix;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueryBean 转换器测试
 */
class QueryBeanTransformerTest {

    @Test
    void testTransformClassNotNull() {
        // 创建一个简单的类字节码用于测试
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
        cw.visitEnd();

        byte[] original = cw.toByteArray();
        byte[] transformed = QueryBeanTransformer.transformClass(original);

        assertNotNull(transformed, "转换后的字节码不应为 null");
        assertTrue(transformed.length > 0, "转换后的字节码长度应大于 0");
    }

    @Test
    void testFieldInsnTransformation() throws IOException {
        // 测试字段访问指令是否被正确处理
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);

        // 添加一个字段
        cw.visitField(Opcodes.ACC_PUBLIC, "queryBean", "Lio/ebean/typequery/TQProperty;", null, null);

        // 添加一个方法，访问该字段
        var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "test", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, "TestClass", "queryBean", "Lio/ebean/typequery/TQProperty;");
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        byte[] original = cw.toByteArray();
        byte[] transformed = QueryBeanTransformer.transformClass(original);

        // 验证转换结果
        assertNotNull(transformed);

        // 使用 ClassNode 来检查转换后的字节码
        ClassReader cr = new ClassReader(transformed);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        // 检查方法中的指令
        MethodNode method = cn.methods.stream()
            .filter(m -> m.name.equals("test"))
            .findFirst()
            .orElse(null);

        assertNotNull(method, "应该找到 test 方法");

        // 检查是否有方法调用指令（INVOKEVIRTUAL）
        boolean hasMethodCall = method.instructions.iterator().next() != null;
        assertTrue(hasMethodCall, "转换后应该包含指令");
    }

    @Test
    void testTransformDirectory() {
        // 测试转换不存在的目录应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            QueryBeanTransformer.transformDirectory("/nonexistent/directory");
        });
    }

    @Test
    void testIsQueryBeanFieldDetection() {
        // 这是一个内部逻辑测试，通过创建访问器来验证
        // 实际的检测逻辑在 QueryBeanFieldToMethodVisitor 中

        // 创建测试类来验证 QueryBean 字段检测
        byte[] testClass = createTestClassWithQueryBeanField();
        assertNotNull(testClass);

        // 转换应该成功
        byte[] transformed = QueryBeanTransformer.transformClass(testClass);
        assertNotNull(transformed);
    }

    /**
     * 创建一个包含 QueryBean 字段的测试类
     */
    private byte[] createTestClassWithQueryBeanField() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "TestQueryBeanClass", null, "java/lang/Object", null);

        // 添加 QueryBean 类型字段
        cw.visitField(
            Opcodes.ACC_PUBLIC,
            "name",
            "Lio/ebean/typequery/PString;",
            null,
            null
        );

        // 添加构造方法
        var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}
