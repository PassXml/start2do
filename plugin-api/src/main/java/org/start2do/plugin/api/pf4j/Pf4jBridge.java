package org.start2do.plugin.api.pf4j;

/**
 * PF4J 桥接编排接口
 * <p>
 * 目的：将“模块能力（MyBatis/Spring MVC/SOAP/...)”抽象为可排序的桥接单元，
 * 由宿主统一编排调用顺序，避免使用 {@code @DependsOn} 这种“依赖必须存在”的硬约束。
 */
public interface Pf4jBridge {

    /**
     * 桥接执行顺序（数字越小越先执行）。
     * <p>
     * 推荐区间：100、200、300...，便于后续插入新模块。
     */
    int getOrder();

    /**
     * 插件启动后触发（按 order 升序调用）。
     */
    void onPluginStarted(String pluginId);

    /**
     * 插件停止/卸载/禁用/失败触发（按 order 降序调用）。
     */
    void onPluginStopped(String pluginId);
}

