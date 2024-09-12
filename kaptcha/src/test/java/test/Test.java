package test;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;
import javax.imageio.ImageIO;

class Test {

    @org.junit.jupiter.api.Test
    void test() throws IOException {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        //验证码是否带边框 No
        properties.setProperty("kaptcha.border", "no");
        //验证码字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        //验证码整体宽度
        properties.setProperty("kaptcha.image.width", "150");
        //验证码整体高度
        properties.setProperty("kaptcha.image.height", "150");
        //文字个数
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        //文字大小
        properties.setProperty("kaptcha.textproducer.font.size", "16");
        //文字随机字体
        properties.setProperty("kaptcha.textproducer.font.names", "Source Han Sans Sc");
        //文字距离
        properties.setProperty("kaptcha.textproducer.char.space", "14");
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.FishEyeGimpy");
        //干扰线颜色
        properties.setProperty("kaptcha.noise.color", "blue");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        BufferedImage bi = defaultKaptcha.createImage("你好世界");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", outputStream);
        System.out.println(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
    }
}
