package org.start2do.plugin.api.spring;

import java.util.Collection;
import org.pf4j.ExtensionPoint;

/**
 * SOAP WebService 扩展点
 * <p>
 * 插件通过实现该接口，声明需要由宿主动态发布的 SOAP 服务实现类及其元数据。
 */
public interface SoapWebServiceExtension extends ExtensionPoint {

    /**
     * 返回需要发布的 SOAP WebService 元数据集合
     */
    Collection<WebServiceMeta> getWebServices();

    /**
     * WebService 元数据
     */
    final class WebServiceMeta {

        /**
         * WebService 实现类（必须带 @javax.jws.WebService 注解）
         */
        private final Class<?> implClass;

        /**
         * 发布地址（相对于 CXF 基础路径），例如："/ws/hello"
         * 为空时由 Bridge 根据注解或类名生成
         */
        private final String address;

        /**
         * JAX-WS serviceName，空则从 @WebService 或类名推导
         */
        private final String serviceName;

        /**
         * JAX-WS portName，空则从 @WebService 或默认规则推导
         */
        private final String portName;

        /**
         * JAX-WS targetNamespace，空则从 @WebService 或默认规则推导
         */
        private final String targetNamespace;

        public WebServiceMeta(Class<?> implClass,
            String address,
            String serviceName,
            String portName,
            String targetNamespace) {
            this.implClass = implClass;
            this.address = address;
            this.serviceName = serviceName;
            this.portName = portName;
            this.targetNamespace = targetNamespace;
        }

        public Class<?> getImplClass() {
            return implClass;
        }

        public String getAddress() {
            return address;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getPortName() {
            return portName;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }
    }
}

