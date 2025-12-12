package org.start2do.plugin.handle;

/**
 * 插件 Controller 路由冲突异常
 * <p>
 * 当插件中的 Controller 请求路径 / HTTP 方法与宿主或其他插件已有映射冲突时抛出，
 * 用于在启用插件（upload(enable)/enable）时向调用方返回详细的冲突信息。
 */
public class PluginControllerMappingConflictException extends RuntimeException {

    public PluginControllerMappingConflictException(String message) {
        super(message);
    }

    public PluginControllerMappingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

