package org.start2do.entity.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.start2do.constant.DBConstant;
import org.start2do.dto.BusinessException;
import org.start2do.ebean.dict.DictItems;
import org.start2do.ebean.dict.IDictItem;
import org.start2do.ebean.entity.BaseModel2;
import org.start2do.util.StringUtils;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "sys_url_permission")
public class SysUrlPermission extends BaseModel2 {

    @EmbeddedId
    private SysUrlPermissionId id;
    @Column(length = DBConstant.URL_LENGTH, insertable = false, updatable = false)
    private String uri;
    @Column(insertable = false, updatable = false)
    private SourceType sourceType;
    @Column(length = DBConstant.ADDRESS_LENGTH, insertable = false, updatable = false)
    private String sourceId;

    public enum SourceType implements IDictItem {
        Role("role", "角色"), User("user", "用户");

        SourceType(String value, String label) {
            putItemBean(value, label);
        }

        @JsonCreator
        public static SourceType get(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            return find(value).orElseThrow(() -> new BusinessException("未知字典值:" + value));
        }

        public static Optional<SourceType> find(String value) {
            SourceType result = DictItems.getByValue(SourceType.class, value);
            if (result == null) {
                return Optional.empty();
            }
            return Optional.of(result);
        }
    }

    public SysUrlPermission(SysUrlPermissionId id) {
        this.id = id;
    }

    public static SysUrlPermission role(String uri, Integer roleId) {
        return new SysUrlPermission(new SysUrlPermissionId(uri, SourceType.Role, roleId.toString()));
    }

    public static SysUrlPermission user(String uri, Integer userId) {
        return new SysUrlPermission(new SysUrlPermissionId(uri, SourceType.User, userId.toString()));
    }
}
