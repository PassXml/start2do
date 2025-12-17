package org.start2do.plugin.env.web;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.start2do.plugin.env.PluginContextHolder;

/**
 * Web 请求期插件上下文绑定：
 * <p>
 * - 若当前 Handler 来自某个插件 ClassLoader，则在请求处理期间绑定 pluginId + TCCL；
 * - 使插件代码在运行时调用 Environment.getProperty(...) 也能读到自身配置（且与其他插件隔离）。
 */
@Slf4j
@RequiredArgsConstructor
public class PluginRequestContextInterceptor implements HandlerInterceptor {

    private static final String ATTR_SET = PluginRequestContextInterceptor.class.getName() + ".set";
    private static final String ATTR_OLD_TCCL = PluginRequestContextInterceptor.class.getName() + ".oldTccl";

    private final PluginManager pluginManager;

    /**
     * 缓存：ClassLoader -> pluginId（弱引用 key，避免阻止插件 ClassLoader 回收）。
     */
    private final ConcurrentReferenceHashMap<ClassLoader, String> pluginIdByClassLoader =
        new ConcurrentReferenceHashMap<>(256, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        Class<?> beanType = hm.getBeanType();
        if (beanType == null) {
            return true;
        }

        ClassLoader beanCl = beanType.getClassLoader();
        if (beanCl == null) {
            return true;
        }

        String pluginId = pluginIdByClassLoader.computeIfAbsent(beanCl, this::resolvePluginIdByClassLoader);
        if (pluginId == null) {
            return true;
        }

        PluginWrapper wrapper = pluginManager.getPlugin(pluginId);
        ClassLoader pluginCl = wrapper == null ? beanCl : wrapper.getPluginClassLoader();
        if (pluginCl == null) {
            pluginCl = beanCl;
        }

        Thread t = Thread.currentThread();
        request.setAttribute(ATTR_SET, Boolean.TRUE);
        request.setAttribute(ATTR_OLD_TCCL, t.getContextClassLoader());

        try {
            t.setContextClassLoader(pluginCl);
            PluginContextHolder.set(pluginId, pluginCl);
        } catch (Exception ex) {
            log.warn("绑定插件请求上下文失败(忽略继续): pluginId={}", pluginId, ex);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object set = request.getAttribute(ATTR_SET);
        if (!(set instanceof Boolean) || !((Boolean) set)) {
            return;
        }

        try {
            Object old = request.getAttribute(ATTR_OLD_TCCL);
            if (old instanceof ClassLoader) {
                Thread.currentThread().setContextClassLoader((ClassLoader) old);
            }
        } finally {
            PluginContextHolder.clear();
        }
    }

    private String resolvePluginIdByClassLoader(ClassLoader cl) {
        if (pluginManager == null || cl == null) {
            return null;
        }
        List<PluginWrapper> plugins = pluginManager.getPlugins();
        if (plugins == null || plugins.isEmpty()) {
            return null;
        }
        for (PluginWrapper w : plugins) {
            if (w == null) {
                continue;
            }
            ClassLoader pluginCl = w.getPluginClassLoader();
            if (pluginCl == null) {
                continue;
            }
            if (pluginCl == cl || isParent(pluginCl, cl)) {
                return w.getPluginId();
            }
        }
        return null;
    }

    private boolean isParent(ClassLoader expectedParent, ClassLoader child) {
        ClassLoader c = child;
        while (c != null) {
            if (c == expectedParent) {
                return true;
            }
            c = c.getParent();
        }
        return false;
    }
}
