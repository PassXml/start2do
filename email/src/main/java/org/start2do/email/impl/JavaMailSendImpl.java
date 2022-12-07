package org.start2do.email.impl;

import java.util.Properties;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.start2do.email.EmailUtils;
import org.start2do.email.config.EmailConfig;
import org.start2do.email.dto.EmailPojo;
import org.start2do.util.BeanValidatorUtil;
import org.start2do.util.StringUtils;

@Component
@Configuration
@RequiredArgsConstructor
@Slf4j
public class JavaMailSendImpl implements EmailUtils {

    private String from;
    private JavaMailSenderImpl mailClient;
    private final EmailConfig config;


    @PostConstruct
    public void init() {
        mailClient = new JavaMailSenderImpl();
        mailClient.setHost(config.getHost());
        mailClient.setPort(config.getPort());
        mailClient.setUsername(config.getUsername());
        mailClient.setPassword(config.getPassword());
        Properties props = mailClient.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        this.from = config.getFrom();
    }


    @SneakyThrows
    public void sendEmail(EmailPojo pojo, Consumer<Void> success, Consumer<Throwable> fail) {
        BeanValidatorUtil.validate(pojo);
        if (StringUtils.isEmpty(pojo.getFrom())) {
            pojo.setFrom(from);
        }
        MimeMessage message = mailClient.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(pojo.getFrom());
        helper.setTo(pojo.getTo().toArray(new String[]{}));
        helper.setSubject(pojo.getSubject());
        helper.setText(pojo.getHtml(), true);
        if (pojo.getAttachment() != null) {
            pojo.getAttachment().set(helper);
        }
        try {
            mailClient.send(message);
            success.accept(null);
        } catch (Throwable e) {
            if (fail != null) {
                fail.accept(e);
            } else {
                log.error(e.getMessage(), e);
            }
        }
    }

}
