package org.start2do.plugin.server.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.plugin.api.dto.ApiResponse;
import org.start2do.plugin.server.model.NodeInfo;
import org.start2do.plugin.server.service.NodeRegistryService;
import org.start2do.plugin.server.web.dto.NodeRegisterRequest;

/**
 * 节点管理接口
 * <p>
 * 多节点无需注册中心，主动向该服务注册与上报心跳。
 */
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeController {

    private final NodeRegistryService nodeRegistryService;

    /**
     * 注册或更新节点信息（也可作为心跳使用）
     */
    @PostMapping("/register")
    public ApiResponse<NodeInfo> register(@RequestBody NodeRegisterRequest request) {
        return ApiResponse.success(nodeRegistryService.register(request));
    }

    /**
     * 查询所有节点状态
     */
    @GetMapping
    public ApiResponse<List<NodeInfo>> listNodes() {
        return ApiResponse.success(nodeRegistryService.listNodes());
    }
}
