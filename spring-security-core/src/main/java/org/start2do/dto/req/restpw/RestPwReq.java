package org.start2do.dto.req.restpw;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.util.StringUtils;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor

public class RestPwReq {

    /**
     * 用户名
     */
    @NotEmpty
    private String username;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 邮件
     */
    private String email;
    /**
     * 找回方式
     */
    @NotNull
    private Type type = Type.Email;

    public enum Type implements IDictItem {
        Email("1", "电子邮件"), SMS("2", "短信");


        Type(String value, String label) {
            putItemBean(value, label);
        }

        @JsonCreator
        public static Type get(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            return find(value).orElseThrow(() -> new BusinessException("未知字典值:" + value));
        }

        public static Optional<Type> find(String value) {
            Type result = DictItems.getByValue(Type.class, value);
            if (result == null) {
                return Optional.empty();
            }
            return Optional.of(result);
        }
    }


}
