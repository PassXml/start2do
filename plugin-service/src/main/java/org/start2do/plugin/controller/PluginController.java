package org.start2do.plugin.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.plugin.api.dto.ApiResponse;
import org.start2do.plugin.dto.PluginInfo;
import org.start2do.plugin.service.PluginFileService;

/**
 * 插件管理接口
 * <p>
 * 提供上传、启用、停用、删除、查询功能
 */
@RestController
@RequestMapping("/plugins")
public class PluginController {

    private final PluginFileService pluginFileService;

    public PluginController(PluginFileService pluginFileService) {
        this.pluginFileService = pluginFileService;
    }

    /**
     * 上传插件文件
     *
     * @param file   jar 文件
     * @param enable 是否立即启用（默认 false）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> upload(@RequestPart("file") MultipartFile file,
        @RequestParam(name = "enable", defaultValue = "false") boolean enable) throws IOException {
        return ApiResponse.success(pluginFileService.upload(file, enable));
    }

    /**
     * 启用插件：根据插件 ID，将 {pluginId-xxx}.jar.disable -> {pluginId-xxx}.jar
     */
    @PostMapping("enable")
    public ApiResponse<String> enable(String pluginId) throws IOException {
        return ApiResponse.success(pluginFileService.enable(pluginId));
    }

    /**
     * 停用插件：{name}.jar -> {name}.jar.disable
     */
    @PostMapping("disable")
    public ApiResponse<Void> disable(String pluginId) throws IOException {
        pluginFileService.disable(pluginId);
        return ApiResponse.success(null);
    }

    /**
     * 删除插件：同时删除启用/禁用文件
     */
    @GetMapping("/delete")
    public ApiResponse<Void> delete(String name) throws IOException {
        pluginFileService.delete(name);
        return ApiResponse.success(null);
    }

    /**
     * 查询插件列表
     */
    @GetMapping(value = {"", "/", "/list"})
    public ApiResponse<List<PluginInfo>> list() {
        return ApiResponse.success(pluginFileService.listPlugins());
    }
}
