package org.start2do.entity.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.ebean.Model;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "sys_user_dept")
public class SysUserDept extends Model {

  @Id @EmbeddedId private SysUserDeptId id;
  private String userId;
  private String deptId;

  private Type type;

  public enum Type implements IDictItem {
    Main("1", "主部门"),
    Sub("2", "挂职");
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

  public SysUserDept(SysUserDeptId id, Type type) {
    this.id = id;
    this.type = type;
  }
}
