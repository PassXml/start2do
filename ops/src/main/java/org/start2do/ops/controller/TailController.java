package org.start2do.ops.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.start2do.ops.config.OpsConfig;
import org.start2do.ops.service.DeployService;
import org.start2do.ops.service.TailService;
import org.start2do.util.FileUtil;
import org.start2do.util.ZipUtil;

@Slf4j
@Controller
@RequestMapping("tail")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "start2do.ops", name = "enable")
public class TailController {

    private final OpsConfig opsConfig;
    private final DeployService deployService;
    private final TailService tailService;

    /**
     * 读取文件
     */
    @GetMapping("tail")
    public SseEmitter tail(@RequestParam String filePath) {
        SseEmitter emitter = new SseEmitter(-1L); // 无超时
        tailService.tailFile(filePath, emitter);
        return emitter;
    }

}
