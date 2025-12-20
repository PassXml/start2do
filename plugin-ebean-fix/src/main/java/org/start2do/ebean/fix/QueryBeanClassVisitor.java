package org.start2do.ebean.fix;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * QueryBean 类转换器
 * 用于转换类中的所有方法，将 QueryBean 字段访问转换为方法调用
 */
public class QueryBeanClassVisitor extends ClassVisitor {

    public QueryBeanClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (mv != null) {
            // 为每个方法应用 QueryBean 字段到方法的转换
            mv = new QueryBeanFieldToMethodVisitor(Opcodes.ASM9, mv);
        }
        return mv;
    }
}
