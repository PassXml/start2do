package org.start2do.email;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.start2do.email.config.EmailConfig;
import org.start2do.email.impl.JavaMailSendImpl;
import org.start2do.email.impl.VertxMailImpl;

@Configuration
@EnableAutoConfiguration
@ConditionalOnProperty(prefix = "email", name = "enable", havingValue = "true")
@Import(EmailConfig.class)
@RequiredArgsConstructor
public class EmailStater {

    private final EmailConfig config;


    @ConditionalOnMissingBean(value = EmailUtils.class)
    @ConditionalOnProperty(prefix = "email", name = "type", havingValue = "javax-mail")
    public EmailUtils JavaMailSendImpl() {
        return new JavaMailSendImpl(config);
    }

    @ConditionalOnMissingBean(value = EmailUtils.class)
    @ConditionalOnProperty(prefix = "email", name = "type", havingValue = "vertx")
    public EmailUtils VertxMailImpl() {
        return new VertxMailImpl(config);
    }

}
