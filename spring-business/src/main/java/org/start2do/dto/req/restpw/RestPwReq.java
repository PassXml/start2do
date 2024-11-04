package org.start2do.dto.req.restpw;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.ebean.dict.IDictItem;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor

public class RestPwReq {

    @NotEmpty
    private String username;
    private String phone;
    private String email;
    @NotEmpty
    private Type type = Type.Email;

    public enum Type implements IDictItem {
        Email("1", "email"), SMS("2", "sms");


        Type(String value, String label) {
            putItemBean(value, label);
        }
    }

}
