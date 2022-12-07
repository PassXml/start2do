package org.start2do.email.impl;


import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.start2do.email.EmailUtils;
import org.start2do.email.config.EmailConfig;
import org.start2do.email.dto.EmailPojo;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.StringUtils;

@Component
@Configuration
@ConditionalOnMissingBean(value = EmailUtils.class)
@ConditionalOnProperty(prefix = "email", name = "type", havingValue = "vertx")
@RequiredArgsConstructor
@Slf4j
public class VertxMailImpl implements EmailUtils {

    private final EmailConfig config;
    private MailClient mailClient;

    private Vertx vertx;

    @PostConstruct
    public void init() {
        vertx = Vertx.vertx(
            new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(false)));
        vertx.exceptionHandler(throwable -> {
            log.error(throwable.getMessage());
            throwable.printStackTrace();
        });
        mailClient = MailClient.create(vertx,
            new MailConfig().setHostname(config.getHost()).setPort(config.getPort()).setUsername(config.getUsername())
                .setPassword(config.getPassword()));
    }

    public void sendEmail(EmailPojo pojo, Consumer<Void> success, Consumer<Throwable> fail) {
        BeanValidatorUtil.validate(true, pojo);
        if (StringUtils.isEmpty(pojo.getFrom())) {
            pojo.setFrom(config.getFrom());
        }
        MailMessage mailMessage = new MailMessage().setFrom(pojo.getFrom()).setTo(pojo.getTo())
            .setSubject(pojo.getSubject()).setHtml(pojo.getHtml());
        if (pojo.getAttachment() != null) {
            pojo.getAttachment().set(mailMessage);
        }
        mailClient.sendMail(mailMessage).onSuccess(event -> {
            success.accept(null);
        }).onFailure(event -> {
            if (fail != null) {
                fail.accept(event);
            } else {
                log.error(event.getMessage(), event);
            }
        });
    }


}
