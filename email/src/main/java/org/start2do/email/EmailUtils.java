package org.start2do.email;


import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.start2do.email.config.EmailConfig;
import org.start2do.email.dto.EmailPojo;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.StringUtils;

@Component
@Configuration
@ConditionalOnProperty(prefix = "email", name = "enable", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class EmailUtils {

    private final EmailConfig config;
    private MailClient mailClient;

    private Vertx vertx;
    private Handler<Throwable> defaultFailHandle = throwable -> {
        log.error("发送邮件失败{}", throwable);
    };

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

    public void sendEmail(EmailPojo pojo, Handler<MailResult> handler, Handler<Throwable> fail) {
        BeanValidatorUtil.validate(true, pojo);
        if (StringUtils.isEmpty(pojo.getFrom())) {
            pojo.setFrom(config.getFrom());
        }
        MailMessage mailMessage = new MailMessage().setFrom(pojo.getFrom()).setTo(pojo.getTo())
            .setSubject(pojo.getSubject()).setHtml(pojo.getHtml());
        if (pojo.getAttachment() != null) {
            mailMessage.setAttachment(pojo.getAttachment());
        }
        if (fail == null) {
            fail = defaultFailHandle;
        }
        mailClient.sendMail(mailMessage).onSuccess(handler).onFailure(fail);
    }


}
