package org.start2do.controller.webflux;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.start2do.BusinessConfig;
import org.start2do.BusinessConfig.FileSetting;
import org.start2do.dto.R;
import org.start2do.dto.resp.file.SysFileUploadResp;
import org.start2do.service.webflux.SysFileReactiveService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 系统文件
 */
@RestController
@RequestMapping("/file")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "file", havingValue = "true")
@ConditionalOnWebApplication(type = Type.REACTIVE)

public class SysFileController {

    private final SysFileReactiveService sysFileService;
    private final BusinessConfig config;
    private final String uploadDir;

    public SysFileController(SysFileReactiveService sysFileService, BusinessConfig config) {
        this.sysFileService = sysFileService;
        this.config = config;
        if (config.getFileSetting() == null) {
            config.setFileSetting(new FileSetting());
        }
        uploadDir = config.getFileSetting().getUploadDir();
        File file = Paths.get(config.getFileSetting().getUploadDir()).toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 下载
     */
    @GetMapping("download")
    public Mono<Void> download(ServerHttpResponse response, @RequestParam String fileId) {
        return sysFileService.getById(fileId).flatMap(sysFile -> {
            response.getHeaders().add("Content-Disposition", "attachment;filename=" + sysFile.getFileName());
            response.getHeaders().add("Content-Type", "application/octet-stream");
            switch (config.getFileSetting().getType()) {
                case local -> {
                    try (FileInputStream inputStream = new FileInputStream(
                        Paths.get(uploadDir + File.separator + sysFile.getFilePath()).toFile())) {
                        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.readByteChannel(inputStream::getChannel,
                            new DefaultDataBufferFactory(), 4096);
                        return response.writeWith(dataBufferFlux);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return Mono.empty();
        });

    }

    /**
     * 根据路径下载
     *
     * @return
     */
    @RequestMapping(value = "download_proxy/**", method = RequestMethod.GET)
    public Mono<Void> downloadByPath(HttpServletRequest request, ServerHttpResponse response) throws IOException {
        String path = request.getRequestURI().replaceFirst("/file/download_proxy", "");
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        response.getHeaders().add("Content-Disposition", "attachment;filename=" + fileName);
        response.getHeaders().add("Content-Type", "application/octet-stream");
        switch (config.getFileSetting().getType()) {
            case local -> {
                try (FileInputStream inputStream = new FileInputStream(
                    Paths.get(uploadDir + File.separator + path).toFile())) {
                    Flux<DataBuffer> dataBufferFlux = DataBufferUtils.readByteChannel(inputStream::getChannel,
                        new DefaultDataBufferFactory(), 4096);
                    return response.writeWith(dataBufferFlux);
                }
            }
        }
        return Mono.empty();
    }

    /**
     * 上传
     */
    @PostMapping("upload")
    public Mono<R<SysFileUploadResp>> upload(MultipartFile file) throws IOException {
        return sysFileService.updateFile(file)
            .map(entity -> new SysFileUploadResp(entity.getId(), entity.getRelativeFilePath())).map(R::ok);
    }
}
