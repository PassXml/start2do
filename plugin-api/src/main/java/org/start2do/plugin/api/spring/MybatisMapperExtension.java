package org.start2do.plugin.api.spring;

import java.util.Collection;
import org.pf4j.ExtensionPoint;

/**
 * MyBatis Mapper 扩展点
 * <p>
 * 插件通过实现该接口，将自身的 Mapper 接口与逻辑数据源 ID 进行绑定，
 * 宿主在运行时基于该信息创建对应的 Mapper 代理并注册为 Spring Bean。
 */
public interface MybatisMapperExtension extends ExtensionPoint {

    /**
     * 返回本插件声明的 Mapper 元信息集合
     */
    Collection<MapperMeta> getMappers();
}

