package org.start2do.plugin.soap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.pf4j.PluginWrapper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.start2do.plugin.api.spring.PluginSpringBeanUtils;
import org.start2do.plugin.api.spring.SoapWebServiceExtension;
import org.start2do.plugin.api.spring.SoapWebServiceExtension.WebServiceMeta;

/**
 * PF4J 与 CXF SOAP 的桥接组件
 * <p>
 * 职责：
 * 1. 监听插件生命周期（启动 / 停止）
 * 2. 在插件启动时，从插件扩展点获取 @WebService 实现类，并通过 CXF 动态发布 Endpoint
 * 3. 在插件停止时，停止并移除对应的 SOAP Endpoint
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Pf4jSoapBridge implements PluginStateListener {

    private final PluginManager pluginManager;
    private final ConfigurableApplicationContext applicationContext;

    /**
     * 使用 CXF 默认 Bus（名称通常为 "cxf"），避免与业务自定义 Bus（如 "bus"）产生歧义。
     */
    private final Bus cxfBus;

    /**
     * 记录每个插件发布过的 SOAP Endpoint，方便卸载时统一停止
     */
    private final Map<String, List<EndpointImpl>> pluginEndpoints = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (pluginManager == null) {
            log.info("Pf4jSoapBridge 初始化: 未找到 PluginManager，跳过 SOAP 动态注册");
            return;
        }

        pluginManager.addPluginStateListener(this);

        // 对已 STARTED 的插件做一次补偿注册
        for (PluginWrapper wrapper : pluginManager.getPlugins()) {
            if (wrapper.getPluginState() == PluginState.STARTED) {
                registerPluginSoapServices(wrapper.getPluginId());
            }
        }

        log.info("Pf4jSoapBridge 初始化完成");
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        String pluginId = event.getPlugin().getPluginId();
        PluginState state = event.getPluginState();
        if (state == PluginState.STARTED) {
            registerPluginSoapServices(pluginId);
        } else if (state == PluginState.UNLOADED || state == PluginState.STOPPED
            || state == PluginState.DISABLED || state == PluginState.FAILED) {
            unregisterPluginSoapServices(pluginId);
        }
    }

    /**
     * 动态发布指定插件声明的所有 SOAP WebService
     */
    private void registerPluginSoapServices(String pluginId) {
        List<SoapWebServiceExtension> extensions =
            pluginManager.getExtensions(SoapWebServiceExtension.class, pluginId);

        if (extensions.isEmpty()) {
            log.info("插件 {} 未提供 SoapWebServiceExtension，跳过 SOAP 注册", pluginId);
            return;
        }

        List<EndpointImpl> endpoints = new ArrayList<>();

        for (SoapWebServiceExtension ext : extensions) {
            for (WebServiceMeta meta : ext.getWebServices()) {
                Class<?> implClass = meta.getImplClass();
                try {
                    String beanName = PluginSpringBeanUtils.buildPluginBeanName(pluginId, implClass);
                    Object serviceBean = applicationContext.getBean(beanName);

                    // 解析 @WebService 注解
                    WebService wsAnn = AnnotationUtils.findAnnotation(implClass, WebService.class);
                    if (wsAnn == null) {
                        throw new IllegalStateException("@WebService 未声明: " + implClass.getName());
                    }

                    // 解析地址与元数据
                    String address = resolveAddress(meta, wsAnn, implClass);
                    String serviceName = resolveServiceName(meta, wsAnn, implClass);
                    String portName = resolvePortName(meta, wsAnn, implClass);
                    String targetNs = resolveTargetNamespace(meta, wsAnn, implClass);

                    EndpointImpl endpoint = new EndpointImpl(cxfBus, serviceBean);
                    if (serviceName != null && targetNs != null) {
                        endpoint.setServiceName(new QName(targetNs, serviceName));
                    }
                    if (portName != null && targetNs != null) {
                        endpoint.setEndpointName(new QName(targetNs, portName));
                    }

                    endpoint.publish(address);

                    endpoints.add(endpoint);
                    log.info("插件 {} 发布 SOAP WebService 成功: class={}, address={}, serviceName={}, portName={}, ns={}",
                        pluginId, implClass.getName(), address, serviceName, portName, targetNs);
                } catch (Exception e) {
                    log.error("插件 {} 发布 SOAP WebService 失败: class={}", pluginId, implClass.getName(), e);
                }
            }
        }

        if (!endpoints.isEmpty()) {
            pluginEndpoints.put(pluginId, endpoints);
        }
    }

    /**
     * 取消发布指定插件的所有 SOAP WebService
     */
    private void unregisterPluginSoapServices(String pluginId) {
        List<EndpointImpl> endpoints = pluginEndpoints.remove(pluginId);
        if (endpoints != null) {
            for (EndpointImpl endpoint : endpoints) {
                try {
                    endpoint.stop();
                } catch (Exception e) {
                    log.warn("插件 {} 停止 SOAP WebService 失败(忽略继续)", pluginId, e);
                }
            }
            log.info("插件 {} 已取消发布 SOAP WebService 数量: {}", pluginId, endpoints.size());
        }
    }

    private String resolveAddress(WebServiceMeta meta, WebService wsAnn, Class<?> implClass) {
        // 1) 元数据显式指定
        if (meta.getAddress() != null && !meta.getAddress().isEmpty()) {
            String addr = meta.getAddress();
            return addr.startsWith("/") ? addr : "/" + addr;
        }
        // 2) 用 serviceName 生成
        String name = wsAnn.serviceName();
        if (name == null || name.isEmpty()) {
            name = implClass.getSimpleName();
        }
        return "/ws/" + name;
    }

    private String resolveServiceName(WebServiceMeta meta, WebService wsAnn, Class<?> implClass) {
        if (meta.getServiceName() != null && !meta.getServiceName().isEmpty()) {
            return meta.getServiceName();
        }
        if (wsAnn.serviceName() != null && !wsAnn.serviceName().isEmpty()) {
            return wsAnn.serviceName();
        }
        return implClass.getSimpleName();
    }

    private String resolvePortName(WebServiceMeta meta, WebService wsAnn, Class<?> implClass) {
        if (meta.getPortName() != null && !meta.getPortName().isEmpty()) {
            return meta.getPortName();
        }
        if (wsAnn.portName() != null && !wsAnn.portName().isEmpty()) {
            return wsAnn.portName();
        }
        return implClass.getSimpleName() + "Port";
    }

    private String resolveTargetNamespace(WebServiceMeta meta, WebService wsAnn, Class<?> implClass) {
        if (meta.getTargetNamespace() != null && !meta.getTargetNamespace().isEmpty()) {
            return meta.getTargetNamespace();
        }
        if (wsAnn.targetNamespace() != null && !wsAnn.targetNamespace().isEmpty()) {
            return wsAnn.targetNamespace();
        }
        // 默认：根据包名反转生成 namespace
        Package pkg = implClass.getPackage();
        if (pkg != null && pkg.getName() != null) {
            String[] parts = pkg.getName().split("\\.");
            StringBuilder sb = new StringBuilder("http://");
            for (int i = parts.length - 1; i >= 0; i--) {
                sb.append(parts[i]);
                if (i > 0) {
                    sb.append(".");
                }
            }
            sb.append("/");
            return sb.toString();
        }
        return "http://default.namespace/";
    }

}
