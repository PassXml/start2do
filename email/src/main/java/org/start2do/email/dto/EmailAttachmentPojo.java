package org.start2do.email.dto;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.mail.MailAttachment;
import io.vertx.ext.mail.MailMessage;
import java.io.ByteArrayOutputStream;
import javax.mail.MessagingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class EmailAttachmentPojo {

    private String fileName;
    private ByteArrayOutputStream outputStream;
    private String contentType;

    public void set(MailMessage message) {
        message.setAttachment(MailAttachment.create().setContentType(this.contentType).setName(this.fileName)
            .setData(Buffer.buffer(outputStream.toByteArray())));
    }

    public void set(MimeMessageHelper helper) {
        try {
            helper.addAttachment(this.fileName, new ByteArrayResource(this.outputStream.toByteArray()),
                this.contentType);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public EmailAttachmentPojo(String fileName, ByteArrayOutputStream outputStream, String contentType) {
        this.fileName = fileName;
        this.outputStream = outputStream;
        this.contentType = contentType;
    }
}
