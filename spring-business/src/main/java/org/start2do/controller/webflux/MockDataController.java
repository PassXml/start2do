package org.start2do.controller.webflux;


import jakarta.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.start2do.dto.req.mock.MockDataImageReq;
import org.start2do.util.MockDataUtil;

/**
 * Mock 数据
 */
@RestController
@RequestMapping("mock")
@ConditionalOnProperty(prefix = "start2do.business.controller", name = "mock", havingValue = "true")
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class MockDataController {


    @GetMapping("image")
    public ResponseEntity image(@Valid MockDataImageReq req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        BufferedImage image = MockDataUtil.mockImageData(req.getFontName(),req.getColor(),req.getWidth(), req.getHeight(), req.getIndex(),req.getText());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", baos);
            baos.flush();
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 返回ResponseEntity
        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

}