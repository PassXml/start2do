package org.start2do.plugin.mybatis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.start2do.MybatisDatasourceFactory;
import org.start2do.plugin.api.pf4j.Pf4jBridge;
import org.start2do.plugin.api.spring.MapperMeta;
import org.start2do.plugin.api.spring.MybatisMapperExtension;

/**
 * PF4J 与 MyBatis Mapper 的桥接组件
 * <p>
 * 职责：
 * 1. 监听插件生命周期（启动 / 停止）
 * 2. 插件启动时，为其声明的 Mapper 创建专用 SqlSessionFactory / SqlSessionTemplate，并生成 Mapper 代理
 * 3. 将 Mapper 代理注册为 Spring Bean，供插件 Service 或宿主直接注入使用
 * 4. 插件停止 / 卸载时，销毁 Mapper Bean 并清理内部缓存，避免类加载器与资源泄漏
 *
 * 重要设计：
 * - 每个插件在每个逻辑 dataSourceId 上拥有独立的 SqlSessionFactory / SqlSessionTemplate，
 *   避免将插件 Mapper 注册到宿主的 MyBatis Configuration 中，从根源降低内存泄漏风险。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Pf4jMybatisMapperBridge implements Pf4jBridge {

    private final PluginManager pluginManager;

    private final ConfigurableApplicationContext applicationContext;

    private final MybatisDataSourceRegistry dataSourceRegistry;

    /**
     * 缓存插件级别的 SqlSessionTemplate：key = pluginId + "#" + dataSourceId
     */
    private final Map<String, SqlSessionTemplate> pluginSqlSessionTemplates = new ConcurrentHashMap<>();

    /**
     * 记录每个插件动态注册的 Mapper Bean 名称，便于卸载时集中销毁。
     */
    private final Map<String, List<String>> pluginMapperBeanNames = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (pluginManager == null) {
            log.info("Pf4jMybatisMapperBridge 初始化: 未找到 PluginManager，跳过插件 Mapper 动态注册");
            return;
        }
        log.info("Pf4jMybatisMapperBridge 初始化完成，将由 Pf4jBridgeOrchestrator 统一编排触发");
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public void onPluginStarted(String pluginId) {
        registerPluginMappers(pluginId);
    }

    @Override
    public void onPluginStopped(String pluginId) {
        unregisterPluginMappers(pluginId);
    }

    /**
     * 插件启动时，为其声明的 Mapper 创建代理并注册为 Spring Bean
     */
    private void registerPluginMappers(String pluginId) {
        List<MybatisMapperExtension> exts =
            pluginManager.getExtensions(MybatisMapperExtension.class, pluginId);

        if (exts.isEmpty()) {
            log.info("插件 {} 未实现 MybatisMapperExtension，跳过 Mapper 注册", pluginId);
            return;
        }

        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        List<String> beanNames = new ArrayList<>();

        for (MybatisMapperExtension ext : exts) {
            for (MapperMeta meta : ext.getMappers()) {
                if (meta == null || meta.getDataSourceId() == null || meta.getMapperClass() == null) {
                    continue;
                }
                String dataSourceId = meta.getDataSourceId().trim();
                Class<?> mapperClass = meta.getMapperClass();
                try {
                    SqlSessionTemplate template = resolveOrCreateTemplate(pluginId, dataSourceId);
                    if (template == null) {
                        log.warn(
                            "插件 {} 注册 Mapper 失败: 未找到 dataSourceId={} 对应的数据源, mapper={}",
                            pluginId, dataSourceId, mapperClass.getName());
                        continue;
                    }

                    // 确保当前 SqlSessionFactory 已经认识该 Mapper 接口：
                    // 1. 对于 XML 形式的 Mapper，前面的 mapperLocations 已经加载了对应的语句，
                    //    但由于 ClassLoader 差异，MyBatis 可能没有自动将 namespace 绑定到接口类型；
                    // 2. 这里显式调用 addMapper，将插件 ClassLoader 中的接口注册到 MapperRegistry。
                    try {
                        SqlSessionFactory sqlSessionFactory = template.getSqlSessionFactory();
                        if (!sqlSessionFactory.getConfiguration().hasMapper(mapperClass)) {
                            sqlSessionFactory.getConfiguration().addMapper(mapperClass);
                            log.info("插件 {} 为数据源 {} 显式注册 Mapper 接口到 SqlSessionFactory: {}",
                                pluginId, dataSourceId, mapperClass.getName());
                        }
                    } catch (Exception e) {
                        log.warn("插件 {} 将 Mapper 接口注册到 SqlSessionFactory 时出现问题(忽略并继续获取代理): dataSourceId={}, mapperClass={}",
                            pluginId, dataSourceId, mapperClass.getName(), e);
                    }

                    Object mapperProxy = template.getMapper(mapperClass);
                    String beanName = buildMapperBeanName(pluginId, dataSourceId, mapperClass);

                    beanFactory.registerSingleton(beanName, mapperProxy);
                    beanNames.add(beanName);

                    log.info("插件 {} 注册 MyBatis Mapper 成功: dataSourceId={}, beanName={}, mapperClass={}",
                        pluginId, dataSourceId, beanName, mapperClass.getName());
                } catch (Exception e) {
                    log.error("插件 {} 注册 MyBatis Mapper 失败: dataSourceId={}, mapperClass={}",
                        pluginId, meta.getDataSourceId(), mapperClass.getName(), e);
                }
            }
        }

        if (!beanNames.isEmpty()) {
            pluginMapperBeanNames.put(pluginId, beanNames);
        }
    }

    /**
     * 插件停止 / 卸载时，销毁其所有 Mapper Bean 并清理 SqlSessionTemplate 缓存
     */
    private void unregisterPluginMappers(String pluginId) {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        List<String> beanNames = pluginMapperBeanNames.remove(pluginId);
        if (beanNames != null) {
            for (String beanName : beanNames) {
                if (!beanFactory.containsSingleton(beanName)) {
                    continue;
                }
                try {
                    // 与 Pf4jSpringMvcBridge 的销毁逻辑保持一致
                    java.lang.reflect.Method destroySingleton =
                        org.springframework.util.ReflectionUtils
                            .findMethod(beanFactory.getClass(), "destroySingleton", String.class);
                    if (destroySingleton != null) {
                        org.springframework.util.ReflectionUtils.makeAccessible(destroySingleton);
                        destroySingleton.invoke(beanFactory, beanName);
                    } else {
                        Object bean = beanFactory.getBean(beanName);
                        beanFactory.destroyBean(bean);
                        java.lang.reflect.Method removeSingleton =
                            org.springframework.util.ReflectionUtils
                                .findMethod(beanFactory.getClass(), "removeSingleton", String.class);
                        if (removeSingleton != null) {
                            org.springframework.util.ReflectionUtils.makeAccessible(removeSingleton);
                            removeSingleton.invoke(beanFactory, beanName);
                        }
                    }
                    log.info("插件 {} 销毁 MyBatis Mapper Bean: {}", pluginId, beanName);
                } catch (Exception e) {
                    log.warn("插件 {} 销毁 MyBatis Mapper Bean 失败(忽略继续): {}", pluginId, beanName, e);
                }
            }
        }

        // 清理该插件相关的 SqlSessionTemplate 缓存，使其与插件类一起被 GC 回收
        String prefix = pluginId + "#";
        pluginSqlSessionTemplates.keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * 解析或按需为插件 + 数据源 ID 创建专用 SqlSessionTemplate
     */
    private SqlSessionTemplate resolveOrCreateTemplate(String pluginId, String dataSourceId) throws Exception {
        String key = pluginId + "#" + dataSourceId;
        SqlSessionTemplate existing = pluginSqlSessionTemplates.get(key);
        if (existing != null) {
            return existing;
        }

        DataSource ds = dataSourceRegistry.resolveDataSource(pluginId, dataSourceId);
        if (ds == null) {
            return null;
        }

        // 为插件构建专用的 mapper XML 与 mybatis-config 资源加载策略，
        // 使用插件自身的 ClassLoader 扫描资源，保证能够读取插件 JAR 内部的 XML。
        Resource[] mapperLocations = null;
        Resource configLocation = null;

        if (pluginManager != null) {
            PluginWrapper wrapper = pluginManager.getPlugin(pluginId);
            if (wrapper != null) {
                ClassLoader pluginClassLoader = wrapper.getPluginClassLoader();
                PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver(pluginClassLoader);

                // 1. 优先尝试加载插件内部的 mapper XML
                try {
                    Resource[] pluginXml =
                        resolver.getResources("classpath*:mybatis/mapper/**/*.xml");
                    if (pluginXml != null && pluginXml.length > 0) {
                        mapperLocations = pluginXml;
                        log.info("插件 {} 使用插件 ClassLoader 加载 MyBatis XML 数量: {}", pluginId, pluginXml.length);
                    }
                } catch (IOException e) {
                    log.warn("插件 {} 扫描 MyBatis Mapper XML 失败，将退回默认注解/宿主配置: {}", pluginId, e.getMessage());
                }

                // 2. 若插件内提供了独立 mybatis-config，则优先使用
                Resource pluginConfig = resolver.getResource("classpath:mybatis/mybatis-config.xml");
                if (pluginConfig != null && pluginConfig.exists()) {
                    configLocation = pluginConfig;
                    log.info("插件 {} 使用插件内 mybatis-config.xml 作为配置文件", pluginId);
                }
            }
        }

        // 使用现有 MybatisDatasourceFactory 创建 SqlSessionFactory。
        // 若 mapperLocations/configLocation 为 null，将退回到宿主的默认扫描与配置策略，
        // 这样支持「仅注解 Mapper」与「插件自带 XML」两种模式。
        SqlSessionFactory factory =
            MybatisDatasourceFactory.sqlSessionFactory(null, ds, mapperLocations, configLocation);

        SqlSessionTemplate template = MybatisDatasourceFactory.sqlSessionTemplate(factory);
        pluginSqlSessionTemplates.put(key, template);
        return template;
    }

    /**
     * 构造插件 Mapper Bean 名称
     */
    private String buildMapperBeanName(String pluginId, String dataSourceId, Class<?> mapperClass) {
        return "plugin_" + pluginId + "_" + dataSourceId + "_" + mapperClass.getName();
    }
}
