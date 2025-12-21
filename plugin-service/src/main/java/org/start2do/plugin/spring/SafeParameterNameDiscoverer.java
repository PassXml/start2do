package org.start2do.plugin.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterNameDiscoverer;
import org.start2do.plugin.env.PluginContextHolder;

/**
 * 安全的参数名解析器（仅在插件上下文中容错）。
 * <p>
 * 背景：PF4J 插件环境下，Spring 默认的 LocalVariableTableParameterNameDiscoverer 可能因为
 * 1) 方法签名涉及的类型在插件 ClassLoader 中不可见；
 * 2) .class 资源与已加载的 Class 不一致（多份同名类/资源加载顺序差异）；
 * 导致抛出 IllegalStateException 进而阻断 Bean 创建或 HandlerMethod 初始化。
 * <p>
 * 处理策略：
 * - 若当前线程已绑定 PluginContextHolder（说明正在处理插件启动/插件请求），则在解析失败时降级返回 null；
 * - 非插件上下文保持原行为（抛出异常），避免掩盖宿主自身问题。
 */
@Slf4j
public class SafeParameterNameDiscoverer implements ParameterNameDiscoverer {

    private final ParameterNameDiscoverer delegate;

    public SafeParameterNameDiscoverer(ParameterNameDiscoverer delegate) {
        this.delegate = delegate;
    }

    @Override
    public String[] getParameterNames(Method method) {
        try {
            return delegate.getParameterNames(method);
        } catch (RuntimeException | LinkageError ex) {
            return handleFailure("method", method == null ? null : method.toGenericString(), ex);
        }
    }

    @Override
    public String[] getParameterNames(Constructor<?> ctor) {
        try {
            return delegate.getParameterNames(ctor);
        } catch (RuntimeException | LinkageError ex) {
            return handleFailure("constructor", ctor == null ? null : ctor.toGenericString(), ex);
        }
    }

    private String[] handleFailure(String kind, String signature, Throwable ex) {
        String pluginId = PluginContextHolder.getPluginId();
        if (pluginId == null) {
            // 非插件上下文：保持 Spring 原本的严格策略，避免掩盖宿主自身问题
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException(ex);
        }

        // 插件上下文：容错降级，避免因“参数名解析”阻断插件启动/请求处理
        String msg = ex.getMessage();
        if (msg != null && msg.contains("cannot be resolved in the class object")) {
            log.warn("插件 {} 参数名解析失败已降级为 null: kind={}, signature={}, msg={}. " +
                    "建议：1) 插件编译开启 -parameters；2) Controller/Bean 构造函数显式 @Autowired 或 @ConstructorProperties；" +
                    "3) 检查 dependencies.txt 与类加载策略，避免同名类多份冲突",
                pluginId, kind, signature, msg);
        } else {
            log.warn("插件 {} 参数名解析异常已降级为 null: kind={}, signature={}, msg={}",
                pluginId, kind, signature, msg);
        }
        return null;
    }
}

