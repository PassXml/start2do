package org.start2do.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.config.KaptchaConfig;
import org.start2do.dto.BusinessException;
import org.start2do.dto.R;
import org.start2do.dto.resp.login.CodeResp;
import org.start2do.util.StringUtils;
import org.start2do.util.spring.LogAop.LogSetting;
import org.start2do.util.spring.RedisCacheUtil;

/**
 * 登录
 */
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
@ConditionalOnProperty(name = "jwt.kaptcha.enable", havingValue = "true")
public class KaptchaController {

    private final DefaultKaptcha kaptchaProducer;
    public static final String KEY = "Cache:Redis:Code:";
    private final KaptchaConfig config;


    /**
     * 验证码
     */
    @SneakyThrows
    @GetMapping("/code")
    @LogSetting(ignore = true)
    public R<CodeResp> code() {
        if (config.getEnable() == null || !config.getEnable()) {
            throw new BusinessException("未启用验证码");
        }
        String text = kaptchaProducer.createText();
        BufferedImage bi = kaptchaProducer.createImage(text);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", outputStream);
        String key = StringUtils.randomString(12);
        String redisKey = KEY + key;
        RedisCacheUtil.set(redisKey, text, 2, TimeUnit.MINUTES);
        return R.ok(new CodeResp(key, Base64.getEncoder().encodeToString(outputStream.toByteArray())));
    }

}
