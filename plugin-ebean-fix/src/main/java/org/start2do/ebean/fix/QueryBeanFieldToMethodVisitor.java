package org.start2do.ebean.fix;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * QueryBean 字段访问转换为方法调用的 MethodVisitor
 * 将 QueryBean 的属性访问（如 qBean.name）转换为方法调用（如 qBean._name()）
 */
public class QueryBeanFieldToMethodVisitor extends MethodVisitor {

    private static final String QUERY_BEAN_BASE_CLASS = "io/ebean/typequery/TQProperty";

    public QueryBeanFieldToMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        // 只处理 GETFIELD 指令（获取字段值）
        if (opcode == Opcodes.GETFIELD && isQueryBeanField(descriptor)) {
            // 将字段访问转换为方法调用
            // GETFIELD owner.name:descriptor -> INVOKEVIRTUAL owner._name()descriptor
            String methodName = "_" + name;
            String methodDescriptor = "()" + descriptor;

            // 调用方法而不是访问字段
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                owner,
                methodName,
                methodDescriptor,
                false
            );
        } else {
            // 其他情况保持原样
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }

    /**
     * 判断字段描述符是否表示 QueryBean 类型
     * QueryBean 字段通常是 TQProperty 的子类
     */
    private boolean isQueryBeanField(String descriptor) {
        if (descriptor == null || descriptor.length() < 3) {
            return false;
        }

        // 检查是否是对象类型（以 L 开头，; 结尾）
        if (!descriptor.startsWith("L") || !descriptor.endsWith(";")) {
            return false;
        }

        // 提取类名
        String className = descriptor.substring(1, descriptor.length() - 1);

        // 检查是否是 QueryBean 相关的类
        // TQProperty 是所有 QueryBean 属性的基类
        return className.startsWith("io/ebean/typequery/")
            || className.contains("query/Q");
    }
}
