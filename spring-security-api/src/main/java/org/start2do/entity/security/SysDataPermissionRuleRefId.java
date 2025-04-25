package org.start2do.entity.security;


import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.util.StringUtils;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class SysDataPermissionRuleRefId implements Serializable {

    @Column(length = DBConstant.UUID_STR_LENGTH)
    private String ruleId;
    private Type sourceType;
    @Column(length = DBConstant.UUID_STR_LENGTH)
    private String sourceId;

    public enum Type implements IDictItem {
        ROLE("1", "角色"),

        USER("2", "用户"), POST("3", "岗位"),

        ;

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
