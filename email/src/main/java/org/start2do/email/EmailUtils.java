package org.start2do.email;


import java.util.function.Consumer;
import org.start2do.email.dto.EmailPojo;

public interface EmailUtils {

    void sendEmail(EmailPojo pojo, Consumer<Void> success, Consumer<Throwable> fail);


}
