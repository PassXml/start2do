package org.start2do.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import java.util.Properties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ConfigurationProperties(prefix = "jwt.kaptcha")
public class KaptchaConfig {

    private Boolean enable;
    private String width = "400";
    private String height = "125";
    private String fontSize = "100";

    @Bean
    @ConditionalOnProperty(prefix = "jwt.kaptcha", name = "enable", havingValue = "true")
    public DefaultKaptcha DefaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        //验证码是否带边框 No
        properties.setProperty("kaptcha.border", "no");
        //验证码字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        //验证码整体宽度
        properties.setProperty("kaptcha.image.width", width);
        //验证码整体高度
        properties.setProperty("kaptcha.image.height", height);
        //文字个数
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        //文字大小
        properties.setProperty("kaptcha.textproducer.font.size", fontSize);
        //文字随机字体
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        //文字距离
        properties.setProperty("kaptcha.textproducer.char.space", "14");
        //干扰线颜色
        properties.setProperty("kaptcha.noise.color", "blue");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

}
