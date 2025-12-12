package com.start2do.test.plugin.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * 示例 SOAP WebService 接口（JAX-WS 风格）
 * <p>
 * 注意：仅定义接口与示例，实际发布由宿主应用的 SOAP 框架（如 CXF、Spring-WS）负责。
 */
@WebService(name = "HelloWebService", targetNamespace = "http://test.plugin.start2do.org/ws")
public interface HelloWebService {

    /**
     * 简单问候接口
     *
     * @param name 名称
     * @return 问候语
     */
    @WebMethod(operationName = "sayHello")
    String sayHello(String name);
}

