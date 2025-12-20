package org.start2do.ebean.fix.gradle

/**
 * Ebean Fix 插件配置
 */
class EbeanFixExtension {
    /**
     * 是否启用 QueryBean 转换，默认启用
     */
    boolean enabled = true

    /**
     * 转换失败时是否中断构建，默认为 true
     */
    boolean failOnError = true

    /**
     * 是否显示详细日志
     */
    boolean verbose = false
}
