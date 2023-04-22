package org.start2do.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.R;
import org.start2do.dto.req.ZLMediaOnServerKeepaliveReq;
import org.start2do.dto.req.ZLMediaOnStreamNoneReaderReq;
import org.start2do.dto.req.ZLMediaOnStreamNotFoundReq;
import org.start2do.service.ZLMediaService;

@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "zl-media-kit", havingValue = "true", name = "web-hook.enable")
@RestController("zlmediakit/hook")
public class ZLMController {

    private final ZLMediaService zlMediaService;

    /**
     * 流未找到.开始拉流
     */
    @PostMapping("on_stream_not_found")
    public R onStreamNotFound(@RequestBody ZLMediaOnStreamNotFoundReq req) {
        zlMediaService.onStreamNotFound(req);
        return R.ok();
    }

    /**
     * 流无人观看时事件，用户可以通过此事件选择是否关闭无人看的流。
     */
    @PostMapping("on_stream_none_reader")
    public R onStreamNoneReader(@RequestBody ZLMediaOnStreamNoneReaderReq req) {
        zlMediaService.onStreamNoneReader(req);
        return R.ok();
    }

    /**
     * 定时上传信息
     */
    @PostMapping("on_server_keepalive")
    public R onServerKeepalive(@RequestBody ZLMediaOnServerKeepaliveReq req) {
        zlMediaService.onServerKeepalive(req);
        return R.ok();
    }
}
