package org.start2do.email.dto;

import java.util.List;
import javax.validation.constraints.NotNull;
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
    private String html;
    private EmailAttachmentPojo attachment;

    public EmailPojo(List<String> to, String subject) {
        this.to = to;
        this.subject = subject;
    }


    public EmailPojo(List<String> to, String from, String subject, String htmlBody) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.html = htmlBody;
    }

    public EmailPojo(List<String> to, String from, String subject) {
        this.to = to;
        this.from = from;
        this.subject = subject;
    }

}
