package com.start2do.test.plugin.ws;

import com.start2do.test.plugin.service.Test2Service;
import javax.annotation.Resource;
import javax.jws.WebService;
import lombok.RequiredArgsConstructor;
import org.start2do.plugin.api.spring.annotation.PluginSoapService;

/**
 * 示例 SOAP WebService 实现类
 * <p>
 * 通过 {@link WebService} 注解声明为 JAX-WS 服务实现， 同时依赖插件内部的 TestService，演示与插件 Service 的协同。
 */
@PluginSoapService
@WebService(
    serviceName = "HelloWebService",
    portName = "HelloWebServicePort",
    targetNamespace = "http://test.plugin.start2do.org/ws",
    // 注意这里要填写接口的“完整限定名”，需要与 HelloWebService.java 的 package 保持一致
    endpointInterface = "com.start2do.test.plugin.ws.HelloWebService"
)
@RequiredArgsConstructor
public class HelloWebServiceImpl implements HelloWebService {

    /**
     * 插件内部 Service，演示与业务逻辑的集成
     */
    @Resource
    private Test2Service testService;

    @Override
    public String sayHello(String name) {
        String version = testService.version();
        return "Hello, " + name + " (from plugin SOAP, version=" + version + ")";
    }
}
