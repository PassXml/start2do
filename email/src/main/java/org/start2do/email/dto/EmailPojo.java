package org.start2do.email.dto;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import io.vertx.ext.mail.MailAttachment;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class EmailPojo {

    @NotNull
    private List<String> to;
    private String from;
    @NotNull
    private String subject;
    @NotEmpty
    private String html;
    private MailAttachment attachment;

    public EmailPojo(List<String> to, String from, String subject, String htmlBody) {
        this.to = to;
        this.from = from;
        this.subject = subject;
    }

    public EmailPojo(List<String> to, String from, String subject) {
        this.to = to;
        this.from = from;
        this.subject = subject;
    }

}
